package org.opennms.keycloak.admin.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.core.util.IOUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;
import org.opennms.keycloak.admin.client.util.SurprisinglyHardToFindUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class KeycloakAdminClientImplTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClientImplTest.class);

    private KeycloakAdminClientImpl target = new KeycloakAdminClientImpl();

    private KeycloakResponseUtil mockKeycloakResponseUtil;
    private ObjectMapper mockObjectMapper;
    private HttpClient mockHttpClient;
    private SurprisinglyHardToFindUtils mockSurprisinglyHardToFindUtils;
    private HttpResponse mockHttpResponse;

    private AccessTokenResponse testAccessTokenResponse;

    public void setUp() throws Exception {
        mockKeycloakResponseUtil = Mockito.mock(KeycloakResponseUtil.class);
        mockObjectMapper = Mockito.mock(ObjectMapper.class);
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockSurprisinglyHardToFindUtils = Mockito.mock(SurprisinglyHardToFindUtils.class);

        mockHttpResponse = Mockito.mock(HttpResponse.class);

        testAccessTokenResponse = new AccessTokenResponse();
        testAccessTokenResponse.setToken("x-access-token-x");
        testAccessTokenResponse.setRefreshToken("x-refresh-token-x");

        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setObjectMapper(mockObjectMapper);
        target.setHttpClient(mockHttpClient);
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);

        target.init();

        Mockito.when(mockSurprisinglyHardToFindUtils.encodeUrlPathSegment("x-realm-x")).thenReturn("x-realm-encoded-x");
    }

    public void testInit() {
        // Use a new target (skip the common setup).
        KeycloakAdminClientImpl initTarget = new KeycloakAdminClientImpl();

        assertNull(initTarget.getObjectMapper());
        assertNull(initTarget.getHttpClient());

        initTarget.init();

        assertNotNull(initTarget.getObjectMapper());
        assertNotNull(initTarget.getHttpClient());
    }

    public void testLogin() throws Exception {
        //
        // Test Setup
        //
        Predicate<HttpEntity> entityMatcher = httpEntity ->
                httpEntityMatchesForm(
                        httpEntity,
                        new BasicNameValuePair("username", "x-username-x"),
                        new BasicNameValuePair("password", "x-password-x"),
                        new BasicNameValuePair("grant_type", "password"),
                        new BasicNameValuePair("client_id", KeycloakAdminClientImpl.DEFAULT_CLIENT_ID),
                        new BasicNameValuePair("scope", KeycloakAdminClientImpl.DEFAULT_SCOPE)
                );

        ArgumentMatcher<HttpUriRequest> httpUriRequestArgumentMatcher =
                httpUriRequest ->
                        httpPostRequestMatches(
                                httpUriRequest,
                                "http://localhost:9999/realms/x-realm-encoded-x/protocol/openid-connect/token",
                                ContentType.APPLICATION_FORM_URLENCODED.getMimeType(),
                                entityMatcher
                        );

        Mockito.when(mockHttpClient.execute(Mockito.argThat(httpUriRequestArgumentMatcher))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseAccessTokenResponse(mockHttpResponse)).thenReturn(testAccessTokenResponse);


        //
        // Test Execution
        //
        target.setBaseUrl("http://localhost:9999");
        target.login("x-realm-x", "x-username-x", "x-password-x");
    }

    public void testLoginException() throws Exception {
        //
        // Test Setup
        //
        Predicate<HttpEntity> entityMatcher = httpEntity ->
                httpEntityMatchesForm(
                        httpEntity,
                        new BasicNameValuePair("username", "x-username-x"),
                        new BasicNameValuePair("password", "x-password-x"),
                        new BasicNameValuePair("grant_type", "password"),
                        new BasicNameValuePair("client_id", KeycloakAdminClientImpl.DEFAULT_CLIENT_ID),
                        new BasicNameValuePair("scope", KeycloakAdminClientImpl.DEFAULT_SCOPE)
                );

        ArgumentMatcher<HttpUriRequest> httpUriRequestArgumentMatcher =
                httpUriRequest ->
                        httpPostRequestMatches(
                                httpUriRequest,
                                "http://localhost:9999/realms/x-realm-encoded-x/protocol/openid-connect/token",
                                ContentType.APPLICATION_FORM_URLENCODED.getMimeType(),
                                entityMatcher
                        );

        Mockito.when(mockHttpClient.execute(Mockito.argThat(httpUriRequestArgumentMatcher))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseAccessTokenResponse(mockHttpResponse)).thenThrow(new KeycloakAuthenticationException("x-kc-auth-exc-x"));


        //
        // Test Execution
        //
        target.setBaseUrl("http://localhost:9999");

        try {
            target.login("x-realm-x", "x-username-x", "x-password-x");
            fail("missing expected exception");
        } catch (KeycloakAuthenticationException kcAuthExc) {
            assertEquals("x-kc-auth-exc-x", kcAuthExc.getMessage());
        }
    }

    private boolean httpPostRequestMatches(HttpUriRequest httpUriRequest, String expectedUri, String expectedContentType, Predicate<HttpEntity> entityMatcher) {
        boolean result = false;
        if (httpUriRequest instanceof HttpPost) {
            if (expectedUri.equals(httpUriRequest.getURI().toString())) {
                Header contentHeader = httpUriRequest.getFirstHeader(HTTP.CONTENT_TYPE);
                if ((contentHeader != null) && (expectedContentType.equals(contentHeader.getValue()))) {
                    if (entityMatcher.test(((HttpPost) httpUriRequest).getEntity())) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    /**
     * WARNING: the method of validating the parameters is not ideal here due to limitations in the UrlEncodedFormEntity
     * class.  It is possible for false positives or negatives, depending on the test values used.
     *
     * @param httpEntity
     * @param expectedParams
     * @return
     * @throws IOException
     */
    private boolean httpEntityMatchesForm(HttpEntity httpEntity, BasicNameValuePair... expectedParams) {
        try {
            boolean result = false;
            if (httpEntity instanceof UrlEncodedFormEntity) {
                UrlEncodedFormEntity urlEncodedFormEntity = (UrlEncodedFormEntity) httpEntity;
                try (InputStream inputStream = urlEncodedFormEntity.getContent()) {
                    // This is a little complicated because UrlEncodedFormEntity does not store, nor provide access to,
                    //  the original parameters - it only stores the already-encoded string.
                    String content = IOUtils.toString(new InputStreamReader(inputStream));

                    Map<String, String> params = splitFormContent(content);

                    result = true;
                    for (BasicNameValuePair onePair : expectedParams) {
                        String value = params.get(onePair.getName());

                        if (! onePair.getValue().equals(value)) {
                            log.warn("Entity form parameter does not match: param-name={}; expected-value={}; actual-value={}", onePair.getName(), onePair.getValue(), value);
                            result = false;
                        }
                    }
                }
            }

            return result;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * NOTE: this implementation is only intended for use in the tests here.  It may be incorrect for many valid inputs,
     * but only needs to be correct for the test data used in this test class.
     *
     * @param content
     * @return
     */
    private Map<String, String> splitFormContent(String content) {
        String[] parts = content.split(Pattern.quote("&"));

        Map<String, String> result = new HashMap<>();
        for (String oneAssignment : parts) {
            String[] nameValue = oneAssignment.split("=", 2);

            if (nameValue.length == 2) {
                result.put(nameValue[0], nameValue[1]);
            } else {
                result.put(oneAssignment, "");
            }
        }

        return result;
    }
}