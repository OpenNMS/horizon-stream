package org.opennms.keycloak.admin.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.opennms.keycloak.admin.client.exc.KeycloakAuthenticationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class KeycloakResponseUtilTest extends TestCase {

    private KeycloakResponseUtil target;

    private HttpResponse mockHttpResponse;
    private StatusLine mockStatusLine;
    private HttpEntity mockHttpEntity;
    private ObjectMapper mockObjectMapper;

    private InputStream testInputStream;
    private AccessTokenResponse testAccessTokenResponse;
    private UserRepresentation testUserRepresentation;
    private UserRepresentation[] testUserRepresentationArray;
    private RoleRepresentation[] testRoleRepresentationArray;

    @Override
    public void setUp() throws Exception {
        //
        // Common Test Setup
        //
        target = new KeycloakResponseUtil();

        mockHttpResponse = Mockito.mock(HttpResponse.class);
        mockStatusLine = Mockito.mock(StatusLine.class);
        mockHttpEntity = Mockito.mock(HttpEntity.class);
        mockObjectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
    }

//========================================
// Access Token Parser Testing
//----------------------------------------

    public void testParseAccessToken200Response() throws Exception {
        testAccessTokenResponse = new AccessTokenResponse();
        commonTestParse200Response(AccessTokenResponse.class, testAccessTokenResponse, target::parseAccessTokenResponse);
    }

    public void testParseAccessToken400Response() throws Exception {
        commonTestParse400Response(AccessTokenResponse.class, target::parseAccessTokenResponse, "login request status 400");
    }

    public void testParseAccessToken200IOExceptionResponse() throws Exception {
        commonTestParse200IOExceptionResponse(AccessTokenResponse.class, target::parseAccessTokenResponse);
    }

//========================================
// User Parser Testing
//----------------------------------------

    public void testParseUser200Response() throws Exception {
        testUserRepresentation = new UserRepresentation();
        commonTestParse200Response(UserRepresentation.class, testUserRepresentation, target::parseUserResponse);
    }

    public void testParseUser400Response() throws Exception {
        commonTestParse400Response(UserRepresentation.class, target::parseUserResponse, "retrieve user status 400");
    }

    public void testParseUser200IOExceptionResponse() throws Exception {
        commonTestParse200IOExceptionResponse(UserRepresentation.class, target::parseUserResponse);
    }

//========================================
// User Array Parser Testing
//----------------------------------------

    public void testParseUserArray200Response() throws Exception {
        testUserRepresentationArray = new UserRepresentation[0];
        commonTestParse200Response(UserRepresentation[].class, testUserRepresentationArray, target::parseUserArrayResponse);
    }

    public void testParseUserArray400Response() throws Exception {
        commonTestParse400Response(UserRepresentation[].class, target::parseUserArrayResponse, "retrieve user(s) status 400");
    }

    public void testParseUserArray200IOExceptionResponse() throws Exception {
        commonTestParse200IOExceptionResponse(UserRepresentation[].class, target::parseUserArrayResponse);
    }

//========================================
// Role Mappings Testing
//----------------------------------------

    public void testParseMapping200Response() throws Exception {
        testUserRepresentationArray = new UserRepresentation[0];
        commonTestParse200Response(UserRepresentation[].class, testUserRepresentationArray, target::parseUserArrayResponse);
    }

    public void testParseMapping400Response() throws Exception {
        commonTestParse400Response(MappingsRepresentation.class, target::parseMappingResponse, "retrieve user role mappings status 400");
    }

    public void testParseMapping200IOExceptionResponse() throws Exception {
        commonTestParse200IOExceptionResponse(MappingsRepresentation.class, target::parseMappingResponse);
    }

//========================================
// Role Array Testing
//----------------------------------------

    public void testParseRoleArray200Response() throws Exception {
        testRoleRepresentationArray = new RoleRepresentation[0];
        commonTestParse200Response(RoleRepresentation[].class, testRoleRepresentationArray, target::parseRoleArrayResponse);
    }

    public void testParseRoleArray400Response() throws Exception {
        commonTestParse400Response(RoleRepresentation[].class, target::parseRoleArrayResponse, "retrieve role by name status 400");
    }

    public void testParseRoleArray200IOExceptionResponse() throws Exception {
        commonTestParse200IOExceptionResponse(RoleRepresentation[].class, target::parseRoleArrayResponse);
    }

//========================================
// Common
//----------------------------------------

    private interface ParseOperation<T,R> {
        R apply(T arg) throws Exception;
    }


    /**
     * Verify the normal, happy path: 200 response with valid JSON text that parses (note the parsing is mocked, so
     * invalid json text is used to further ensure the test is functioning as-expected instead of testing an actual
     * json parser).
     *
     * @throws Exception
     */
    private <T> void commonTestParse200Response(Class<T> modelClazz, T testModelInstance, ParseOperation<HttpResponse, T> operation) throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        String jsonResponseText = "x-json-text-x";
        testInputStream = new ByteArrayInputStream(jsonResponseText.getBytes(StandardCharsets.UTF_8));

        Mockito.when(mockHttpEntity.getContent()).thenReturn(testInputStream);
        Mockito.when(mockObjectMapper.readValue(testInputStream, modelClazz)).thenReturn(testModelInstance);

        //
        // Execute
        //
        target.setObjectMapper(mockObjectMapper);
        T response = operation.apply(mockHttpResponse);

        //
        // Validate
        //
        assertNotNull(response);
        assertSame(response, testModelInstance);
    }

    /**
     * Verify handling of a 400 (BAD REQUEST) response.  Expected result is a thrown exception.
     *
     * @throws Exception
     */
    private <T> void commonTestParse400Response(Class<T> modelClazz, ParseOperation<HttpResponse, T> operation, String expectedExceptionText) throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        //
        // Execute
        //
        KeycloakAuthenticationException thrown = null;
        target.setObjectMapper(mockObjectMapper);
        try {
            operation.apply(mockHttpResponse);
        } catch (KeycloakAuthenticationException kaExc) {
            thrown = kaExc;
        }

        //
        // Validate
        //
        assertNotNull("missing expected thrown exception", thrown);
        assertEquals(expectedExceptionText, thrown.getMessage());
    }

    /**
     * Validate the response when a 200 response throws an IOException when parsing the response body.  The expected
     * result is an exception.
     *
     * @throws Exception
     */
    public <T> void commonTestParse200IOExceptionResponse(Class<T> modelClazz, ParseOperation<HttpResponse, T> operation) throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        String jsonResponseText = "x-json-text-x";
        testInputStream = new ByteArrayInputStream(jsonResponseText.getBytes(StandardCharsets.UTF_8));
        IOException testException = new IOException("x-io-exception-x");

        Mockito.when(mockHttpEntity.getContent()).thenReturn(testInputStream);
        Mockito.when(mockObjectMapper.readValue(testInputStream, modelClazz)).thenThrow(testException);

        //
        // Execute
        //
        target.setObjectMapper(mockObjectMapper);

        KeycloakAuthenticationException thrown = null;
        try {
            operation.apply(mockHttpResponse);
        } catch (KeycloakAuthenticationException kaExc) {
            thrown = kaExc;
        }

        //
        // Validate
        //
        assertNotNull(thrown);
        assertSame(testException, thrown.getCause());
        assertEquals("failed to parse response body", thrown.getMessage());
    }
}