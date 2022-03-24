package org.opennms.rest.jaxrs.jwt;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.TokenVerifier;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.rotation.HardcodedPublicKeyLocator;
import org.keycloak.adapters.rotation.JWKPublicKeyLocator;
import org.keycloak.adapters.rotation.PublicKeyLocator;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.keycloak.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import static org.keycloak.TokenVerifier.IS_ACTIVE;
import static org.keycloak.TokenVerifier.SUBJECT_EXISTS_CHECK;

/**
 * JAX-RS Feature for integration with Keycloak.
 *
 * - Validates tokens against Keycloak's public key
 * - Also validates other contents of the tokens using logic from Keycloak's library
 * - Supports 2 sources for loading the public key for token validation:
 *      - Use of a static Public Key loaded from file at startup
 *      - HTTP call to the Keycloak server to load the key (using the Keycloak library)
 */
@Priority(10)  // Make sure to keep this < the Priority of the RolesAllowed feature so this one runs first
public class KeycloakJaxrsFeature implements ContainerRequestFilter {

    public static final String BEARER_TOKEN_SCHEME  = "Bearer ";
    public static final String PEM_KEY_BEGIN_MARKER = "-----BEGIN PUBLIC KEY-----";
    public static final String PEM_KEY_END_MARKER   = "-----END PUBLIC KEY-----";

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(KeycloakJaxrsFeature.class);

    private Logger log = DEFAULT_LOGGER;

    // Injectables
    private boolean tokenSignatureUseStaticKey = false;
    private String tokenSignaturePublicKeyPath;
    private String keycloakServerUrl;
    private String keycloakRealm;
    private boolean keycloakRequireSsl = false;
    private RoleAssignmentProvider roleAssignmentProvider;
    private String issuerUrl;

    // Runtime
    private KeycloakDeployment keycloakDeployment;

//========================================
// Getters and Setters
//----------------------------------------

    public boolean isTokenSignatureUseStaticKey() {
        return tokenSignatureUseStaticKey;
    }

    public void setTokenSignatureUseStaticKey(boolean tokenSignatureUseStaticKey) {
        this.tokenSignatureUseStaticKey = tokenSignatureUseStaticKey;
    }

    public String getTokenSignaturePublicKeyPath() {
        return tokenSignaturePublicKeyPath;
    }

    public void setTokenSignaturePublicKeyPath(String tokenSignaturePublicKeyPath) {
        this.tokenSignaturePublicKeyPath = tokenSignaturePublicKeyPath;
    }

    public String getKeycloakServerUrl() {
        return keycloakServerUrl;
    }

    public void setKeycloakServerUrl(String keycloakServerUrl) {
        this.keycloakServerUrl = keycloakServerUrl;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public void setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
    }

    public boolean isKeycloakRequireSsl() {
        return keycloakRequireSsl;
    }

    public void setKeycloakRequireSsl(boolean keycloakRequireSsl) {
        this.keycloakRequireSsl = keycloakRequireSsl;
    }

    public RoleAssignmentProvider getRoleAssignmentProvider() {
        return roleAssignmentProvider;
    }

    public void setRoleAssignmentProvider(RoleAssignmentProvider roleAssignmentProvider) {
        this.roleAssignmentProvider = roleAssignmentProvider;
    }

    public String getIssuerUrl() {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl) {
        this.issuerUrl = issuerUrl;
    }


//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        try {
            AdapterConfig adapterConfig = configureKeycloak();

            keycloakDeployment = new KeycloakDeployment();
            keycloakDeployment.setAuthServerBaseUrl(adapterConfig);
            keycloakDeployment.setRealm(keycloakRealm);

            // Create the HTTP Client that the Keycloak library will use to call out for Public Key lookup
            HttpClient httpClient = HttpClientBuilder.create().build();
            keycloakDeployment.setClient(httpClient);

            //
            // Configure the Public Key Locator for Keycloak signature validation
            //
            if (tokenSignatureUseStaticKey) {
                configureStaticPublicKeyFile();
            } else {
                configureKeycloakServerPublicKeyLookup();
            }
        } catch (Exception exc) {
            throw new RuntimeException("Failed to initialize the JWT Public Key Signature Verification", exc);
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (keycloakDeployment == null) {
            throw new RuntimeException("Not Initialized - make sure the init() method is called at startup");
        }

        String authHeader = containerRequestContext.getHeaders().getFirst("Authorization");

        if (authHeader != null) {
            if (authHeader.startsWith(BEARER_TOKEN_SCHEME)) {
                String token = authHeader.substring(BEARER_TOKEN_SCHEME.length()).trim();

                AccessToken accessToken = validateAndParseKeycloakToken(containerRequestContext, token);

                if (accessToken != null) {
                    List<String> roleList = roleAssignmentProvider.lookupUserRoles(accessToken.getPreferredUsername());

                    SecurityContext securityContext =
                            new OpennmsUserRolesSecurityContext(accessToken.getPreferredUsername(), roleList);

                    containerRequestContext.setSecurityContext(securityContext);
                } else {
                    log.debug("Token parsing + validation failed");
                    containerRequestContext.abortWith(
                            Response.status(Response.Status.UNAUTHORIZED).build()
                    );
                }
            } else {
                log.debug("Missing Bearer authorization");
                containerRequestContext.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED).build()
                );
            }
        } else {
            log.debug("Missing authorization header");
            containerRequestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }

        containerRequestContext.getRequest();
    }

//========================================
// Internals
//----------------------------------------

    /**
     * Configure the Keycloak "deployment" - this is our client configuration for talking to the Keycloak server.
     * @return
     */
    private AdapterConfig configureKeycloak() {
        AdapterConfig adapterConfig = new AdapterConfig();

        adapterConfig.setAllowAnyHostname(true);
        adapterConfig.setAuthServerUrl(keycloakServerUrl);
        adapterConfig.setRealm(keycloakRealm);
        adapterConfig.setUseResourceRoleMappings(false);
        adapterConfig.setPrincipalAttribute("preferred_username");
        adapterConfig.setSslRequired(Boolean.toString(keycloakRequireSsl));

        return adapterConfig;
    }

    private void configureKeycloakServerPublicKeyLookup() {
        JWKPublicKeyLocator jwkPublicKeyLocator = new JWKPublicKeyLocator();

        keycloakDeployment.setPublicKeyLocator(jwkPublicKeyLocator);
        keycloakDeployment.setPublicKeyCacheTtl(3600);
    }

    private void configureStaticPublicKeyFile () {
        PublicKey publicKey = loadPemPublicKeyFile(tokenSignaturePublicKeyPath);

        PublicKeyLocator publicKeyLocator = new HardcodedPublicKeyLocator(publicKey);
        keycloakDeployment.setPublicKeyLocator(publicKeyLocator);

    }

    private PublicKey loadPemPublicKeyFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(path));
            String fileContentText = new String(bytes, StandardCharsets.UTF_8);

            String extractedKeyText =
                    fileContentText
                            .replace(PEM_KEY_BEGIN_MARKER, "")
                            .replace(PEM_KEY_END_MARKER, "")
                            .trim()
                    ;

            byte[] publicKeyBinary = Base64.decodeBase64(extractedKeyText);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBinary);

            PublicKey result = keyFactory.generatePublic(keySpec);

            return result;
        } catch (Exception exc) {
            throw new RuntimeException("Failed to load PublicKey from file " + path, exc);
        }
    }

    private AccessToken validateAndParseKeycloakToken(ContainerRequestContext containerRequestContext, String tokenString) {
        try {
            //
            // Copied from withDefaultChecks, replacing the always-fails RealmUrlCheck(null) with one that will pass.
            //  Note this is only necessary when avoiding connection to the Keycloak server - the code path when
            //  connecting to the KC server populates the realm url (aka "iss", or issuer).
            //
            // There's some unfortunate choices in the Keycloak library regarding API public and non-public methods
            //  leading to more code here.  Without the Realm URL Check problem, the following line does all of the
            //  same work:
            //
            //     AccessToken result = AdapterTokenVerifier.verifyToken(tokenString, keycloakDeployment);
            //

            // Create the verifier using the Keycloak library.
            TokenVerifier<AccessToken> verifier =
                    AdapterTokenVerifier.createVerifier(tokenString, keycloakDeployment, false, AccessToken.class);

            // Update the checks used by the verifier using the same verifiers as the defaults, with the fix mentioned above.
            verifier.withChecks(
                    new TokenVerifier.RealmUrlCheck(issuerUrl),
                    SUBJECT_EXISTS_CHECK,
                    new TokenVerifier.TokenTypeCheck(TokenUtil.TOKEN_TYPE_BEARER),
                    IS_ACTIVE
            );

            // Verify now
            verifier.verify();

            return verifier.getToken();
        } catch (Exception exc) {
            log.info("JWT authentication failure", exc);
            return null;
        }
    }
}
