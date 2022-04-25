package org.opennms.keycloak.admin.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.mockito.invocation.InvocationOnMock;
import org.opennms.keycloak.admin.client.exc.KeycloakOperationException;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenOp;
import org.opennms.keycloak.admin.client.refresh.RefreshTokenResponse;
import org.opennms.keycloak.admin.client.util.SurprisinglyHardToFindUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import static org.opennms.keycloak.admin.client.impl.KeycloakAdminClientSessionImpl.BEARER_TOKEN_HEADER_SCHEME;

public class KeycloakAdminClientSessionImplTest extends TestCase {

    private KeycloakAdminClientSessionImpl target;

    private SurprisinglyHardToFindUtils mockSurprisinglyHardToFindUtils;
    private KeycloakResponseUtil mockKeycloakResponseUtil;
    private HttpUriRequest mockHttpUriRequest;
    private HttpResponse mockHttpResponse;
    private HttpResponse mockHttpResponse2;
    private HttpResponse mockHttpResponse3;
    private HttpClient mockHttpClient;
    private StatusLine mockStatusLine;
    private Logger mockLogger;
    private ObjectMapper mockObjectMapper;
    private HttpClientRetryUtil mockHttpClientRetryUtil;
    private KeycloakSessionTokenManager mockKeycloakSessionTokenManager;
    private AccessTokenResponse testAccessTokenResponse;

    private UserRepresentation testUserRepresentation;
    private UserRepresentation[] testUserRepresentationArray;
    private MappingsRepresentation testMappingsRepresentation;
    private RoleRepresentation[] testRoleRepresentationArray;

    private RefreshTokenResponse refreshOpResult;

    @Override
    public void setUp() throws Exception {
        target = new KeycloakAdminClientSessionImpl();

        mockSurprisinglyHardToFindUtils = Mockito.mock(SurprisinglyHardToFindUtils.class);
        mockKeycloakResponseUtil = Mockito.mock(KeycloakResponseUtil.class);
        mockHttpUriRequest = Mockito.mock(HttpUriRequest.class);
        mockHttpResponse = Mockito.mock(HttpResponse.class);
        mockHttpResponse2 = Mockito.mock(HttpResponse.class);
        mockHttpResponse3 = Mockito.mock(HttpResponse.class);
        mockHttpClient = Mockito.mock(HttpClient.class);
        mockStatusLine = Mockito.mock(StatusLine.class);
        mockLogger = Mockito.mock(Logger.class);
        mockObjectMapper = Mockito.mock(ObjectMapper.class);
        mockHttpClientRetryUtil = Mockito.mock(HttpClientRetryUtil.class);
        mockKeycloakSessionTokenManager = Mockito.mock(KeycloakSessionTokenManager.class);

        //
        // Common Test Setup
        //
        Mockito.when(mockSurprisinglyHardToFindUtils.encodeUrlPathSegment("x-realm-x")).thenReturn("x-realm-encoded-x");
        Mockito.when(mockSurprisinglyHardToFindUtils.encodeUrlPathSegment("x-user-id-x")).thenReturn("x-user-id-encoded-x");

        Mockito.when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        // Retry Util: by default, just call the HttpClient execute method
        Mockito
                .when(mockHttpClientRetryUtil.executeWithRetry(Mockito.eq(mockHttpClient), Mockito.any(HttpUriRequest.class), Mockito.any(PostRequestOp.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, HttpClient.class).execute(invocationOnMock.getArgument(1, HttpUriRequest.class)))
        ;

        Mockito.when(mockKeycloakSessionTokenManager.getAccessToken()).thenReturn("x-access-token-x");
        Mockito.when(mockKeycloakSessionTokenManager.getRefreshToken()).thenReturn("x-refresh-token-x");
    }

    public void testGetUserByUsernameHappyPath() throws Exception {
        //
        // Test Setup
        //
        testUserRepresentationArray = new UserRepresentation[1];
        testUserRepresentationArray[0] = new UserRepresentation();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseUserArrayResponse(mockHttpResponse)).thenReturn(testUserRepresentationArray);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        UserRepresentation result = target.getUserByUsername("x-realm-x", "x-username-x");


        //
        // Validation
        //
        assertSame(testUserRepresentationArray[0], result);
        Mockito.verifyNoInteractions(mockLogger);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/?username=x-username-x", HttpGet.class);
    }

    public void testGetUserByUsername2MatchesPath() throws Exception {
        //
        // Test Setup
        //
        testUserRepresentationArray = new UserRepresentation[2];
        testUserRepresentationArray[0] = new UserRepresentation();
        testUserRepresentationArray[1] = new UserRepresentation();

        testUserRepresentationArray[0].setId("x-user-id-0-x");
        testUserRepresentationArray[1].setId("x-user-id-1-x");

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseUserArrayResponse(mockHttpResponse)).thenReturn(testUserRepresentationArray);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setLog(mockLogger);
        target.init();
        UserRepresentation result = target.getUserByUsername("x-realm-x", "x-username-x");


        //
        // Validation
        //
        assertSame(testUserRepresentationArray[0], result);
        Mockito.verify(mockLogger).warn("lookup user by username returned {} entries; using the first (id={})", 2, "x-user-id-0-x");

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/?username=x-username-x", HttpGet.class);
    }

    public void testGetUserByUsername0MatchesPath() throws Exception {
        //
        // Test Setup
        //
        testUserRepresentationArray = new UserRepresentation[0];

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseUserArrayResponse(mockHttpResponse)).thenReturn(testUserRepresentationArray);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setLog(mockLogger);
        target.init();
        UserRepresentation result = target.getUserByUsername("x-realm-x", "x-username-x");


        //
        // Validation
        //
        assertNull(result);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/?username=x-username-x", HttpGet.class);
    }

    public void testGetUserById() throws Exception {
        //
        // Test Setup
        //
        testUserRepresentation = new UserRepresentation();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseUserResponse(mockHttpResponse)).thenReturn(testUserRepresentation);


        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        UserRepresentation result = target.getUserById("x-realm-x", "x-user-id-x");


        //
        // Validation
        //
        assertSame(testUserRepresentation, result);
        Mockito.verifyNoInteractions(mockLogger);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/x-user-id-encoded-x", HttpGet.class);
    }

    public void testGetUserRoleMappings() throws Exception {
        //
        // Test Setup
        //
        testMappingsRepresentation = new MappingsRepresentation();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseMappingResponse(mockHttpResponse)).thenReturn(testMappingsRepresentation);


        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        MappingsRepresentation result = target.getUserRoleMappings("x-realm-x", "x-user-id-x");


        //
        // Validation
        //
        assertSame(testMappingsRepresentation, result);
        Mockito.verifyNoInteractions(mockLogger);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/x-user-id-encoded-x/role-mappings", HttpGet.class);
    }

    public void testAddRealm() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(RealmRepresentation.class))).thenReturn("x-json-body-x");

        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setObjectMapper(mockObjectMapper);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.addRealm("x-realm-x", null);


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<RealmRepresentation> realmRepresentationArgumentCaptor =
                ArgumentCaptor.forClass(RealmRepresentation.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(realmRepresentationArgumentCaptor.capture());
        RealmRepresentation realmRepresentation = realmRepresentationArgumentCaptor.getValue();
        assertEquals("x-realm-x", realmRepresentation.getRealm());
        assertEquals(Boolean.TRUE, realmRepresentation.isEnabled());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms", HttpPost.class);
    }

    public void testAddRealmWithCustomizer() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(RealmRepresentation.class))).thenReturn("x-json-body-x");

        Consumer<RealmRepresentation> customizer =
                realmRepresentation -> realmRepresentation.setDisplayName("x-display-name-x");

        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setObjectMapper(mockObjectMapper);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.addRealm("x-realm-x", customizer);


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<RealmRepresentation> realmRepresentationArgumentCaptor =
                ArgumentCaptor.forClass(RealmRepresentation.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(realmRepresentationArgumentCaptor.capture());
        RealmRepresentation realmRepresentation = realmRepresentationArgumentCaptor.getValue();
        assertEquals("x-realm-x", realmRepresentation.getRealm());
        assertEquals(Boolean.TRUE, realmRepresentation.isEnabled());
        assertEquals("x-display-name-x", realmRepresentation.getDisplayName());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms", HttpPost.class);
    }

    public void testAddUser() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(UserRepresentation.class))).thenReturn("x-json-body-x");

        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setObjectMapper(mockObjectMapper);
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.addUser("x-realm-x", "x-username-x", null);


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<UserRepresentation> userRepresentationArgumentCaptor =
                ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(userRepresentationArgumentCaptor.capture());
        UserRepresentation userRepresentation = userRepresentationArgumentCaptor.getValue();
        assertEquals("x-username-x", userRepresentation.getUsername());
        assertEquals(Boolean.TRUE, userRepresentation.isEnabled());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users", HttpPost.class);
    }

    public void testAddUserWithCustomizer() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(UserRepresentation.class))).thenReturn("x-json-body-x");

        Consumer<UserRepresentation> customizer = userRepresentation -> userRepresentation.setEmail("x-email-x");

        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setObjectMapper(mockObjectMapper);
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.addUser("x-realm-x", "x-username-x", customizer);


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<UserRepresentation> userRepresentationArgumentCaptor =
                ArgumentCaptor.forClass(UserRepresentation.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(userRepresentationArgumentCaptor.capture());
        UserRepresentation userRepresentation = userRepresentationArgumentCaptor.getValue();
        assertEquals("x-username-x", userRepresentation.getUsername());
        assertEquals(Boolean.TRUE, userRepresentation.isEnabled());
        assertEquals("x-email-x", userRepresentation.getEmail());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users", HttpPost.class);
    }

    public void testGetRoleByNameHappyPath() throws Exception {
        //
        // Test Setup
        //
        testRoleRepresentationArray = new RoleRepresentation[1];
        testRoleRepresentationArray[0] = new RoleRepresentation();

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseRoleArrayResponse(mockHttpResponse)).thenReturn(testRoleRepresentationArray);


        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setLog(mockLogger);
        target.init();
        RoleRepresentation result = target.getRoleByName("x-realm-x", "x-role-name-x");


        //
        // Validation
        //
        assertSame(testRoleRepresentationArray[0], result);
        Mockito.verifyNoInteractions(mockLogger);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/roles?search=x-role-name-x", HttpGet.class);
    }

    public void testGetRoleByName2MatchesPath() throws Exception {
        //
        // Test Setup
        //
        testRoleRepresentationArray = new RoleRepresentation[2];
        testRoleRepresentationArray[0] = new RoleRepresentation();
        testRoleRepresentationArray[1] = new RoleRepresentation();

        testRoleRepresentationArray[0].setId("x-role-id0-x");
        testRoleRepresentationArray[1].setId("x-role-id1-x");

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseRoleArrayResponse(mockHttpResponse)).thenReturn(testRoleRepresentationArray);


        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setLog(mockLogger);
        target.init();
        RoleRepresentation result = target.getRoleByName("x-realm-x", "x-role-name-x");


        //
        // Validation
        //
        assertSame(testRoleRepresentationArray[0], result);
        Mockito.verify(mockLogger).warn("get role by name query returned multiple matches: rolename={}, count={}; using id={}", "x-role-name-x", 2, "x-role-id0-x");

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/roles?search=x-role-name-x", HttpGet.class);
    }

    public void testGetRoleByName0MatchesPath() throws Exception {
        //
        // Test Setup
        //
        testRoleRepresentationArray = new RoleRepresentation[0];

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockSurprisinglyHardToFindUtils.encodeUrlPathSegment("x-realm-x")).thenReturn("x-realm-encoded-x");
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseRoleArrayResponse(mockHttpResponse)).thenReturn(testRoleRepresentationArray);


        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setLog(mockLogger);
        target.init();
        RoleRepresentation result = target.getRoleByName("x-realm-x", "x-role-name-x");


        //
        // Validation
        //
        assertSame(null
                , result);
        Mockito.verifyNoInteractions(mockLogger);

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/roles?search=x-role-name-x", HttpGet.class);
    }

    public void testCreateRole() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(RoleRepresentation.class))).thenReturn("x-json-body-x");

        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setObjectMapper(mockObjectMapper);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.createRole("x-realm-x", "x-role-name-x");


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<RoleRepresentation> roleRepresentationArgumentCaptor =
                ArgumentCaptor.forClass(RoleRepresentation.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(roleRepresentationArgumentCaptor.capture());
        RoleRepresentation roleRepresentation = roleRepresentationArgumentCaptor.getValue();
        assertEquals("x-role-name-x", roleRepresentation.getName());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/roles", HttpPost.class);
    }

    public void testAssignUserRole() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any(List.class))).thenReturn("x-json-body-x");


        //
        // Execution
        //
        target.setHttpClient(mockHttpClient);
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setObjectMapper(mockObjectMapper);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setBaseUrl("http://localhost:9999");
        target.init();
        target.assignUserRole("x-realm-x", "x-user-id-x", "x-role-name-x", "x-role-id-x");


        //
        // Validation
        //
        Mockito.verifyNoInteractions(mockLogger);

        ArgumentCaptor<List<RoleRepresentation>> roleRepresentationArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mockObjectMapper).writeValueAsString(roleRepresentationArgumentCaptor.capture());

        List<RoleRepresentation> roleRepresentationList = roleRepresentationArgumentCaptor.getValue();
        assertEquals(1, roleRepresentationList.size());

        RoleRepresentation roleRepresentation = roleRepresentationList.get(0);
        assertEquals("x-role-name-x", roleRepresentation.getName());
        assertEquals("x-role-id-x", roleRepresentation.getId());

        // Verify the URL used in the request
        validateRequestTypeAndUri("http://localhost:9999/admin/realms/x-realm-encoded-x/users/x-user-id-encoded-x/role-mappings/realm", HttpPost.class);
    }

    public void testLogout() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseMappingResponse(mockHttpResponse)).thenReturn(testMappingsRepresentation);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setAdminRealm("x-realm-x");
        target.setInitialAccessToken("x-access-token-x");
        target.init();
        target.logout();


        //
        // Validation
        //

        // Verify the URL used in the request
        HttpUriRequest request =
            validateRequestTypeAndUri("http://localhost:9999/realms/x-realm-encoded-x/protocol/openid-connect/logout", HttpGet.class);

        assertEquals("Bearer x-access-token-x", request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
    }

    public void testLogoutFail() throws Exception {
        //
        // Test Setup
        //
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);
        Mockito.when(mockKeycloakResponseUtil.parseMappingResponse(mockHttpResponse)).thenReturn(testMappingsRepresentation);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setAdminRealm("x-realm-x");
        target.setInitialAccessToken("x-access-token-x");
        target.init();

        KeycloakOperationException keycloakOperationException = null;
        try {
            target.logout();
        } catch (KeycloakOperationException koExc) {
            keycloakOperationException = koExc;
        }


        //
        // Validation
        //
        assertNotNull("missing expected exception on HTTP request failure", keycloakOperationException);
        assertEquals("logout status " + HttpStatus.SC_UNAUTHORIZED, keycloakOperationException.getMessage());

        // Verify the URL used in the request
        HttpUriRequest request =
            validateRequestTypeAndUri("http://localhost:9999/realms/x-realm-encoded-x/protocol/openid-connect/logout", HttpGet.class);

        assertEquals("Bearer x-access-token-x", request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
    }

    public void testRefreshTokenWhileGetUserById() throws Exception {
        //
        // Test Setup
        //
        testUserRepresentation = new UserRepresentation();

        testAccessTokenResponse = new AccessTokenResponse();
        testAccessTokenResponse.setToken("x-access-token-2-x");
        testAccessTokenResponse.setRefreshToken("x-refresh-token-2-x");

        // The 2 Get User by Id requests are the only HttpGets
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse, mockHttpResponse2);

        // Token Refresh Request is the only HttpPost
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenReturn(mockHttpResponse3);
        Mockito.when(mockKeycloakResponseUtil.parseUserResponse(mockHttpResponse2)).thenReturn(testUserRepresentation);
        Mockito.when(mockKeycloakResponseUtil.parseAccessTokenResponse(mockHttpResponse3)).thenReturn(testAccessTokenResponse);

        // Retry Util: call the post request operation
        Mockito
                .when(mockHttpClientRetryUtil.executeWithRetry(Mockito.eq(mockHttpClient), Mockito.any(HttpUriRequest.class), Mockito.any(PostRequestOp.class)))
                .thenAnswer(this::answerTestRetryUtilPostOpExecution)
        ;

        // Session Token Manager: call the refresh token op
        Mockito
                .when(mockKeycloakSessionTokenManager.refreshToken(Mockito.eq("x-access-token-x"), Mockito.any(RefreshTokenOp.class)))
                .thenAnswer(this::answerTestRefreshOp)
        ;

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(401);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.init();
        UserRepresentation result = target.getUserById("x-realm-x", "x-user-id-x");


        //
        // Validation
        //
        assertSame(testUserRepresentation, result);
        Mockito.verify(mockHttpUriRequest).setHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_HEADER_SCHEME + "x-access-token-2-x");
    }

    public void testRefreshTokenExceptionWhileGetUserById() throws Exception {
        //
        // Test Setup
        //
        RuntimeException testException = new RuntimeException("x-refresh-token-fail-x");
        testUserRepresentation = new UserRepresentation();

        testAccessTokenResponse = new AccessTokenResponse();
        testAccessTokenResponse.setToken("x-access-token-2-x");
        testAccessTokenResponse.setRefreshToken("x-refresh-token-2-x");

        // The 2 Get User by Id requests are the only HttpGets
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse, mockHttpResponse2);

        // Token Refresh Request is the only HttpPost
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class))).thenThrow(testException);
        Mockito.when(mockKeycloakResponseUtil.parseUserResponse(mockHttpResponse2)).thenReturn(testUserRepresentation);
        Mockito.when(mockKeycloakResponseUtil.parseAccessTokenResponse(mockHttpResponse3)).thenReturn(testAccessTokenResponse);

        // Retry Util: call the refresh operation
        Mockito
                .when(mockHttpClientRetryUtil.executeWithRetry(Mockito.eq(mockHttpClient), Mockito.any(HttpUriRequest.class), Mockito.any(PostRequestOp.class)))
                .thenAnswer(this::answerTestRetryUtilPostOpExecution)
        ;

        Mockito
                .when(mockKeycloakSessionTokenManager.refreshToken(Mockito.eq("x-access-token-x"), Mockito.any(RefreshTokenOp.class)))
                .thenAnswer(this::answerTestRefreshOp)
        ;

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(401);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.init();

        Exception caught = null;
        try {
            UserRepresentation result = target.getUserById("x-realm-x", "x-user-id-x");
            fail("missing expected exception");
        } catch (Exception actualExc) {
            caught = actualExc;
        }


        //
        // Validation
        //
        assertNotNull(caught);
        assertNotNull(caught.getCause());
        assertEquals("failed to refresh token", caught.getMessage());
        assertEquals("failed callout to keycloak in order to refresh the token", caught.getCause().getMessage());
        assertSame(testException, caught.getCause().getCause());
    }

    public void testNoRefreshTokenOn500WhileGetUserById() throws Exception {
        //
        // Test Setup
        //
        RuntimeException testException = new RuntimeException("x-refresh-token-fail-x");
        testUserRepresentation = new UserRepresentation();

        testAccessTokenResponse = new AccessTokenResponse();
        testAccessTokenResponse.setToken("x-access-token-2-x");
        testAccessTokenResponse.setRefreshToken("x-refresh-token-2-x");

        // The 2 Get User by Id requests are the only HttpGets
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpGet.class))).thenReturn(mockHttpResponse);

        // Token Refresh Request is the only HttpPost
        Mockito.when(mockKeycloakResponseUtil.parseUserResponse(mockHttpResponse)).thenThrow(testException);
        Mockito.when(mockKeycloakResponseUtil.parseAccessTokenResponse(mockHttpResponse3)).thenReturn(testAccessTokenResponse);

        // Retry Util: call the refresh operation
        Mockito
                .when(mockHttpClientRetryUtil.executeWithRetry(Mockito.eq(mockHttpClient), Mockito.any(HttpUriRequest.class), Mockito.any(PostRequestOp.class)))
                .thenAnswer(this::answerTestRetryUtilPostOpExecution)
        ;

        Mockito
                .when(mockKeycloakSessionTokenManager.refreshToken(Mockito.eq("x-access-token-x"), Mockito.any(RefreshTokenOp.class)))
                .thenAnswer(this::answerTestRefreshOp)
        ;

        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(500);

        //
        // Execution
        //
        target.setSurprisinglyHardToFindUtils(mockSurprisinglyHardToFindUtils);
        target.setKeycloakResponseUtil(mockKeycloakResponseUtil);
        target.setHttpClientRetryUtil(mockHttpClientRetryUtil);
        target.setHttpClient(mockHttpClient);
        target.setBaseUrl("http://localhost:9999");
        target.setKeycloakSessionTokenManager(mockKeycloakSessionTokenManager);
        target.init();

        Exception caught = null;
        try {
            UserRepresentation result = target.getUserById("x-realm-x", "x-user-id-x");
            fail("missing expected exception");
        } catch (Exception actualExc) {
            caught = actualExc;
        }


        //
        // Validation
        //
        assertNotNull(caught);
        assertSame(testException, caught);
    }


//========================================
// Internal
//----------------------------------------

    private HttpUriRequest validateRequestTypeAndUri(String expectedUri, Class clazz) throws Exception {
        //
        // Verify the URL used in the request
        //
        ArgumentCaptor<HttpUriRequest> httpGetArgumentCaptor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.verify(mockHttpClient).execute(httpGetArgumentCaptor.capture());
        HttpUriRequest httpUriRequest = httpGetArgumentCaptor.getValue();

        assertEquals(new URI(expectedUri), httpUriRequest.getURI());
        assertEquals(clazz, httpUriRequest.getClass());

        return httpUriRequest;
    }

    private Object answerTestRetryUtilPostOpExecution(InvocationOnMock invocationOnMock) throws IOException {
        HttpClient httpClient = invocationOnMock.getArgument(0, HttpClient.class);
        HttpUriRequest httpUriRequest = invocationOnMock.getArgument(1, HttpUriRequest.class);

        HttpResponse httpResponse = httpClient.execute(httpUriRequest);

        PostRequestOp postRequestOp = invocationOnMock.getArgument(2, PostRequestOp.class);
        boolean retryInd = postRequestOp.process(mockHttpUriRequest, httpResponse);

        if (retryInd) {
            httpResponse = httpClient.execute(httpUriRequest);
        }

        return httpResponse;
    }

    private Object answerTestRefreshOp(InvocationOnMock invocationOnMock) {
        RefreshTokenOp refreshTokenOp = invocationOnMock.getArgument(1, RefreshTokenOp.class);

        // Exercise the actual refresh operation
        refreshOpResult = refreshTokenOp.refreshToken();

        return refreshOpResult.getAccessToken();
    }
}