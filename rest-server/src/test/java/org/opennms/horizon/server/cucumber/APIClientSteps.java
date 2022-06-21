/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.server.cucumber;

import static graphql.Assert.assertTrue;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.Given;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class APIClientSteps {
    private static final RestAssuredConfig restConfig = RestAssuredConfig.config().httpClient(
            HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 30000)
                    .setParam("http.socket.timeout", 30000)
    );
    private static final String LOGIN_DATA_TEMPLATE = "client_id=%s&username=%s&password=%s&grant_type=password";
    private static final String LOGIN_URL_TEMPLATE = "%s/realms/%s/protocol/openid-connect/token";
    public static final String PATH_LOCATIONS = "/locations";
    public static final String PATH_NODS = "/nodes";
    public static final String PATH_GRAPHQL = "/graphql";
    public static final String PATH_USERS = "/users";

    private final ObjectMapper mapper = new ObjectMapper();
    private  String clientId;

    protected String apiUrl;
    protected String keycloakAuthUrl;
    private String testRealm;

    private String accessToken;

    private String username;
    private String password;



    @Given("REST server url in system property {string}")
    public void restServerUrlInSystemProperty(String apiUrlProperty) {
        this.apiUrl = System.getProperty(apiUrlProperty);
    }

    @Given("Keycloak auth server url in system property {string}, realm {string} and client {string}")
    public void keycloakAuthServerUrlInSystemProperty(String authUrlProperty, String realm, String clientId) {
        this.keycloakAuthUrl = System.getProperty(authUrlProperty);
        this.testRealm = realm;
        this.clientId = clientId;
    }

    @Given("User {string} with password {string}")
    public void userWithPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Then("User can loging and create access token")
    public void userCanLogingAndCreateAccessToken() {
        assertTrue(login(username, password));
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    protected boolean login(String user, String password) {
        accessToken = "";
       String postData = String.format(LOGIN_DATA_TEMPLATE, clientId, user, password);
        Map<String, String> headers = Map.of(
                "Accept", "application/json",
                "Content-Type", "application/x-www-form-urlencoded");
        Response response = postHelp(null, String.format(LOGIN_URL_TEMPLATE, keycloakAuthUrl, testRealm), restConfig, headers, postData);
        assertEquals(200, response.statusCode());
        accessToken = "Bearer " + response.jsonPath().get("access_token");
        return StringUtils.hasLength(accessToken);
    }

    protected Response postRequest(String path, JsonNode data) {
        return postRequest(path, data.toString());
    }

    protected Response postRequest(String path, String data) {
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json",
                "Authorization", accessToken);
        return postHelp(apiUrl, path, null, headers, data);
    }

    private Response postHelp(String baseUri, String path, RestAssuredConfig config, Map<String, String> headers, String data) {
        RequestSpecification request = given();
        request.headers(headers).body(data);
        if(baseUri != null) {
            request.baseUri(baseUri);
        }
        if(config != null) {
            request.config(config);
        }
        return request.when().post(path).then().extract().response();
    }


    protected Response getRequest(String path) {
        return given()
                .accept(ContentType.JSON)
                .header("Authorization", accessToken)
                .baseUri(apiUrl)
                .when()
                .get(path).then().extract().response();
    }

    protected Response putRequest(String path, JsonNode data) {
        return given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .baseUri(apiUrl)
                .body(data)
                .when()
                .put(path).then().extract().response();
    }

    protected Response deleteRequest(String path) {
        return given()
                .accept(ContentType.JSON)
                .header("Authorization", accessToken)
                .baseUri(apiUrl)
                .when()
                .delete(path).then().extract().response();
    }

    protected void cleanDB() {
        login("admin-user", "password123");
        List<Long> ids = getRequest(PATH_LOCATIONS).jsonPath().getList("id", Long.class);
        ids.forEach(id -> deleteRequest(PATH_LOCATIONS + "/" + id));
    }

    protected JsonNode objectToJson(Object data) {
        return mapper.valueToTree(data);
    }

    protected ObjectNode createJsonNode() {
        return mapper.createObjectNode();
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void verifyWthJsonPath(Response response, List<String> pathValue) {
        assertEquals(pathValue.get(1), response.jsonPath().getString(pathValue.get(0)), "json path:" + pathValue.get(0));
    }

    public String getUserIDFromToken() throws VerificationException {
        AccessToken token = TokenVerifier.create(accessToken.substring(7), AccessToken.class).getToken();
        return token.getSubject();
    }
}
