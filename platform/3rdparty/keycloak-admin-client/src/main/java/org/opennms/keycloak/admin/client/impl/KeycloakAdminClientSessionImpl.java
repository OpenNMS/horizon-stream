package org.opennms.keycloak.admin.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.keycloak.admin.client.KeycloakAdminClientSession;
import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;
import org.opennms.keycloak.admin.client.exc.KeycloakBaseException;
import org.opennms.keycloak.admin.client.exc.KeycloakOperationException;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenResponse;
import org.opennms.keycloak.admin.client.util.SurprisinglyHardToFindUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Concurrency note: concurrent access will result in concurrent calls into the HttpClient class.  The default
 * instances of HttpClient use a thread-safe client connection manager.  If the HttpClient instance, which is injected
 * here, is customized in a way that impacts its thread-safety, the thread-safety of those instances will be impacted.
 *
 * Refresh Token Handling: when a request returns status code 401, the refresh token is used to request an updated
 * Access Token from Keycloak, and on success, a new request is sent with the updated Access Token.  Note that only
 * one retry per request is attempted.
 */
public class KeycloakAdminClientSessionImpl implements KeycloakAdminClientSession {

    public static final String BEARER_TOKEN_HEADER_SCHEME = "Bearer ";

    private static final Logger DEFAULT_LOGGER = org.slf4j.LoggerFactory.getLogger(KeycloakAdminClientSessionImpl.class);

    private final Object lock = new Object();

    private Logger log = DEFAULT_LOGGER;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    private String clientId;
    private String scope;

    private String baseUrl;
    private String adminRealm;
    private String initialAccessToken;
    private String initialRefreshToken;

    private SurprisinglyHardToFindUtils surprisinglyHardToFindUtils = SurprisinglyHardToFindUtils.INSTANCE;
    private HttpClientRetryUtil httpClientRetryUtil = HttpClientRetryUtil.INSTANCE;
    private KeycloakResponseUtil keycloakResponseUtil = KeycloakResponseUtil.INSTANCE;
    private KeycloakSessionTokenManager keycloakSessionTokenManager;


//========================================
// Getters and Setters
//----------------------------------------

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAdminRealm() {
        return adminRealm;
    }

    public void setAdminRealm(String adminRealm) {
        this.adminRealm = adminRealm;
    }

    public String getInitialAccessToken() {
        return initialAccessToken;
    }

    public void setInitialAccessToken(String initialAccessToken) {
        this.initialAccessToken = initialAccessToken;
    }

    public String getInitialRefreshToken() {
        return initialRefreshToken;
    }

    public void setInitialRefreshToken(String initialRefreshToken) {
        this.initialRefreshToken = initialRefreshToken;
    }

    public SurprisinglyHardToFindUtils getSurprisinglyHardToFindUtils() {
        return surprisinglyHardToFindUtils;
    }

    public void setSurprisinglyHardToFindUtils(SurprisinglyHardToFindUtils surprisinglyHardToFindUtils) {
        this.surprisinglyHardToFindUtils = surprisinglyHardToFindUtils;
    }

    public HttpClientRetryUtil getHttpClientRetryUtil() {
        return httpClientRetryUtil;
    }

    public void setHttpClientRetryUtil(HttpClientRetryUtil httpClientRetryUtil) {
        this.httpClientRetryUtil = httpClientRetryUtil;
    }

    public KeycloakResponseUtil getKeycloakResponseUtil() {
        return keycloakResponseUtil;
    }

    public void setKeycloakResponseUtil(KeycloakResponseUtil keycloakResponseUtil) {
        this.keycloakResponseUtil = keycloakResponseUtil;
    }

    public KeycloakSessionTokenManager getKeycloakSessionTokenManager() {
        return keycloakSessionTokenManager;
    }

    public void setKeycloakSessionTokenManager(KeycloakSessionTokenManager keycloakSessionTokenManager) {
        this.keycloakSessionTokenManager = keycloakSessionTokenManager;
    }

//========================================
// Lifecycle
//----------------------------------------

    public void init() {
        if (keycloakSessionTokenManager == null) {
            keycloakSessionTokenManager = new KeycloakSessionTokenManager(initialAccessToken, initialRefreshToken);
        }
    }

//========================================
// API
//----------------------------------------


    @Override
    public UserRepresentation getUserByUsername(String realm, String username) throws IOException, URISyntaxException, KeycloakAuthenticationException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String path = "/admin/realms/" + encodedRealm + "/users/?username=" + encodedUsername;
        URL fullUrl = this.formatUrl(path);

        HttpResponse httpResponse = sendJsonGetWithAuth(fullUrl);

        UserRepresentation[] userRepresentationArray = keycloakResponseUtil.parseUserArrayResponse(httpResponse);
        UserRepresentation result = null;

        if ((userRepresentationArray != null) && (userRepresentationArray.length > 0)) {
            result = userRepresentationArray[0];

            if  (userRepresentationArray.length > 1) {
                log.warn("lookup user by username returned {} entries; using the first (id={})", userRepresentationArray.length, result.getId());
            }
        }

        return result;
    }

    /**
     * Lookup a user from Keycloak given the user's ID (not username).
     *
     * @param userId - the ID (uuid) for the user; note this is NOT the username.
     * @return
     */
    @Override
    public UserRepresentation getUserById(String realm, String userId) throws IOException, URISyntaxException, KeycloakAuthenticationException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        String encodedUserId = surprisinglyHardToFindUtils.encodeUrlPathSegment(userId);
        String path = "/admin/realms/" + encodedRealm + "/users/" + encodedUserId;
        URL fullUrl = this.formatUrl(path);

        HttpResponse httpResponse = sendJsonGetWithAuth(fullUrl);

        UserRepresentation userRepresentation = keycloakResponseUtil.parseUserResponse(httpResponse);

        return userRepresentation;
    }

    @Override
    public MappingsRepresentation getUserRoleMappings(String realm, String userId) throws IOException, URISyntaxException, KeycloakBaseException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        String encodedUserId = surprisinglyHardToFindUtils.encodeUrlPathSegment(userId);
        String path = "/admin/realms/" + encodedRealm + "/users/" + encodedUserId + "/role-mappings";
        URL fullUrl = this.formatUrl(path);

        HttpResponse httpResponse = sendJsonGetWithAuth(fullUrl);

        MappingsRepresentation mappings = keycloakResponseUtil.parseMappingResponse(httpResponse);

        return mappings;
    }

    @Override
    public void addRealm(String realm, Consumer<RealmRepresentation> realmCustomizer) throws IOException, URISyntaxException, KeycloakBaseException {
        URL fullUrl = this.formatUrl("/admin/realms");

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(realm);
        realmRepresentation.setEnabled(true);

        if (realmCustomizer != null) {
            realmCustomizer.accept(realmRepresentation);
        }

        sendJsonPostWithAuth("add realm", fullUrl, realmRepresentation);
    }

    @Override
    public void addUser(String realm, String username, Consumer<UserRepresentation> userCustomizer) throws IOException, URISyntaxException, KeycloakBaseException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        URL fullUrl = this.formatUrl("/admin/realms/" + encodedRealm + "/users");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(username);
        userRepresentation.setEnabled(true);

        if (userCustomizer != null) {
            userCustomizer.accept(userRepresentation);
        }

        sendJsonPostWithAuth("add user", fullUrl, userRepresentation);
    }

    @Override
    public RoleRepresentation getRoleByName(String realm, String roleName) throws IOException, URISyntaxException, KeycloakBaseException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        String encodedRoleName = URLEncoder.encode(roleName, StandardCharsets.UTF_8);
        String path = "/admin/realms/" + encodedRealm + "/roles?search=" + encodedRoleName;
        URL fullUrl = this.formatUrl(path);

        HttpResponse httpResponse = sendJsonGetWithAuth(fullUrl);

        RoleRepresentation[] roleRepresentationMatches = keycloakResponseUtil.parseRoleArrayResponse(httpResponse);

        RoleRepresentation result = null;
        if (roleRepresentationMatches != null) {
            if (roleRepresentationMatches.length > 0) {
                result = roleRepresentationMatches[0];

                if (roleRepresentationMatches.length > 1) {
                    log.warn("get role by name query returned multiple matches: rolename={}, count={}; using id={}",
                        roleName, roleRepresentationMatches.length, result.getId());
                }
            }
        }

        return result;
    }

    @Override
    public void createRole(String realm, String roleName) throws IOException, URISyntaxException, KeycloakBaseException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        URL fullUrl = this.formatUrl("/admin/realms/" + encodedRealm + "/roles");

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);

        sendJsonPostWithAuth("create realm role ", fullUrl, roleRepresentation);
    }

    @Override
    public void assignUserRole(String realm, String userId, String roleName, String roleId) throws IOException, URISyntaxException, KeycloakBaseException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(realm);
        String encodedUserId = surprisinglyHardToFindUtils.encodeUrlPathSegment(userId);
        URL fullUrl = this.formatUrl("/admin/realms/" + encodedRealm + "/users/" + encodedUserId + "/role-mappings/realm");

        List<RoleRepresentation> roles = new LinkedList<>();

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(roleName);
        roleRepresentation.setId(roleId);

        roles.add(roleRepresentation);

        sendJsonPostWithAuth("assign user role", fullUrl, roles);
    }

    @Override
    public void logout() throws IOException, URISyntaxException, KeycloakOperationException {
        String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(adminRealm);
        URL fullUrl = formatUrl("/realms/" + encodedRealm + "/protocol/openid-connect/logout");

        HttpGet httpGet = new HttpGet();
        httpGet.setURI(fullUrl.toURI());
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_HEADER_SCHEME + keycloakSessionTokenManager.getAccessToken());

        // Don't retry - if the access token is already expired, there is no need to logout
        HttpResponse httpResponse = httpClient.execute(httpGet);

        int statusCode = httpResponse.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {
            throw new KeycloakOperationException("logout status " + statusCode);
        }
    }

//========================================
// Internals
//----------------------------------------

    private URL formatUrl(String path) throws MalformedURLException {
        URL base = new URL(baseUrl);
        URL fullUrl = new URL(base, path);

        return fullUrl;
    }

    private HttpEntity prepareRefreshRequestEntity(String refreshToken) throws UnsupportedEncodingException {
        List<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("scope", scope));

        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params);

        return urlEncodedFormEntity;
    }

    private RefreshTokenResponse refreshToken() {
        try {
            //
            // Format the URL
            //
            String encodedRealm = surprisinglyHardToFindUtils.encodeUrlPathSegment(adminRealm);
            String path = "/realms/" + encodedRealm + "/protocol/openid-connect/token";
            URL fullUrl = this.formatUrl(path);

            HttpEntity httpEntity = prepareRefreshRequestEntity(initialRefreshToken);

            HttpPost tokenPostRequest = new HttpPost();
            tokenPostRequest.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            tokenPostRequest.setURI(fullUrl.toURI());
            tokenPostRequest.setEntity(httpEntity);

            // NOTE: this is the token refresh, so directly execute it.  The retry utility does not apply here because it
            //  retries by calling this method.
            HttpResponse httpResponse = httpClient.execute(tokenPostRequest);

            AccessTokenResponse accessTokenResponse = keycloakResponseUtil.parseAccessTokenResponse(httpResponse);

            //
            // Update the tokens
            //
            return new RefreshTokenResponse(
                    accessTokenResponse.getToken(),
                    accessTokenResponse.getRefreshToken());
        } catch (Exception exc) {
            throw new RuntimeException("failed callout to keycloak in order to refresh the token", exc);
        }
    }

    private boolean refreshTokenOn401(HttpUriRequest originalRequest, HttpResponse httpResponse, String usedAccessToken) {
        if (httpResponse.getStatusLine().getStatusCode() == 401) {

            try {
                String updatedAccessToken = keycloakSessionTokenManager.refreshToken(usedAccessToken, this::refreshToken);

                // Update the request with the new access token.
                originalRequest.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_HEADER_SCHEME + updatedAccessToken);

                return true;
            } catch (Exception exc) {
                throw new RuntimeException("failed to refresh token", exc);
            }
        }

        return false;
    }

    private HttpResponse sendJsonGetWithAuth(URL fullUrl) throws URISyntaxException, IOException {
        // Take a snapshot of the Access Token in case it changes due to concurrency
        String usedAccessToken = keycloakSessionTokenManager.getAccessToken();

        HttpGet httpGet = new HttpGet();
        httpGet.setURI(fullUrl.toURI());
        httpGet.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
        httpGet.setHeader("Authorization", BEARER_TOKEN_HEADER_SCHEME + usedAccessToken);

        return
                httpClientRetryUtil
                        .executeWithRetry(
                                httpClient,
                                httpGet,
                                (request, response) -> this.refreshTokenOn401(request, response, usedAccessToken)
                        );
    }

    private <T> HttpResponse sendJsonPostWithAuth(String operationName, URL url, T body) throws IOException, URISyntaxException, KeycloakBaseException {
        String bodyJson = objectMapper.writeValueAsString(body);

        // Take a snapshot of the Access Token in case it changes due to concurrency
        String usedAccessToken = keycloakSessionTokenManager.getAccessToken();

        HttpPost httpPost = new HttpPost();
        httpPost.setURI(url.toURI());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_HEADER_SCHEME + usedAccessToken);
        httpPost.setEntity(new StringEntity(bodyJson));

        HttpResponse httpResponse =
                httpClientRetryUtil.executeWithRetry(
                        httpClient,
                        httpPost,
                        (request, response) -> this.refreshTokenOn401(request, response, usedAccessToken)
                );

        int statusCode = httpResponse.getStatusLine().getStatusCode();

        if ((statusCode / 100) != 2) {
            throw new KeycloakOperationException(operationName + " at url " + url + " failed with status code " + statusCode);
        }

        return httpResponse;
    }
}
