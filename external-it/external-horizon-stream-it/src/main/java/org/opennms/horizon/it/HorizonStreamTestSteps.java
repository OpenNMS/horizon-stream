package org.opennms.horizon.it;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.keycloak.OAuth2Constants.PASSWORD;

public class HorizonStreamTestSteps {

    public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 15_000;

    private static final Logger log = LoggerFactory.getLogger(HorizonStreamTestSteps.class);

    private String horizonStreamBaseUrl;
    private String keycloakBaseUrl;
    private String keycloakRealm;
    private String keycloakUsername;
    private String keycloakPassword;

    private Keycloak keycloakClient;
    private Response restResponse;
    private String keycloakToken;

//========================================
// Test Step Definitions
//----------------------------------------

    @Given("horizon stream server base url in environment variable {string}")
    public void horizonStreamServerBaseUrlInEnvironmentVariable(String variableName) {
        horizonStreamBaseUrl = System.getenv(variableName);

        log.info("HORIZON STREAM BASE URL: {}", horizonStreamBaseUrl);
    }

    @Given("Keycloak server base url in environment variable {string}")
    public void keycloakServerBaseUrlInEnvironmentVariable(String variableName) {
        keycloakBaseUrl = System.getenv(variableName);

        log.info("KEYCLOAK BASE URL: {}", keycloakBaseUrl);
    }

    @Given("Keycloak realm in environment variable {string}")
    public void keycloakRealmInEnvironmentVariable(String variableName) {
        keycloakRealm = System.getenv(variableName);

        log.info("KEYCLOAK REALM: {}", keycloakRealm);
    }

    @Given("Keycloak username in environment variable {string}")
    public void keycloakUsernameInEnvironmentVariable(String variableName) {
        keycloakUsername = System.getenv(variableName);

        log.info("KEYCLOAK USERNAME: {}", keycloakUsername);
    }

    @Given("Keycloak password in environment variable {string}")
    public void keycloakPasswordInEnvironmentVariable(String variableName) {
        keycloakPassword = System.getenv(variableName);
    }

    @Then("login to Keycloak")
    public void loginToKeycloak() {
        keycloakClient =
                KeycloakBuilder.builder()
                    .serverUrl(keycloakBaseUrl)
                    .grantType(PASSWORD)
                    .realm(keycloakRealm)
                    .username(keycloakUsername)
                    .password(keycloakPassword)
                    .clientId("admin-cli") // TBD
                    .build()
        ;

        AccessTokenResponse accessTokenResponse = keycloakClient.tokenManager().getAccessToken();
        keycloakToken = accessTokenResponse.getToken();

        assertNotNull(keycloakToken);
    }

    @Then("send GET request to horizon-stream at path {string}")
    public void sendGETRequestToHorizonStreamAtPath(String path) throws MalformedURLException {
        URL requestUrl = new URL(new URL(horizonStreamBaseUrl), path);

        RestAssuredConfig restAssuredConfig = createRestAssuredTestConfig();

        RequestSpecification requestSpecification =
                RestAssured
                    .given()
                    .config(restAssuredConfig)
                    ;

        if (keycloakToken != null) {
            requestSpecification.header(HttpHeaders.AUTHORIZATION, "Bearer " + keycloakToken);
        }

        restResponse =
                requestSpecification
                        .get(requestUrl)
                        .thenReturn()
        ;
    }

    @Then("verify HTTP response code = {int}")
    public void verifyHTTPResponseCode(int expectedResponseCode) {
        assertEquals(expectedResponseCode, restResponse.getStatusCode());
    }

    @Then("verify response has Minion location = {string}")
    public void verifyMinionResponse(String location) {
        assertTrue(restResponse.getBody().print().contains(location));
    }

//========================================
// Internals
//----------------------------------------

    private RestAssuredConfig createRestAssuredTestConfig() {
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                        .setParam("http.socket.timeout", DEFAULT_HTTP_SOCKET_TIMEOUT)
                );
    }
}
