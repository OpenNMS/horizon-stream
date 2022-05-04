package org.opennms.horizon.it;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class HorizonStreamTestSteps {

    public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 15_000;

    private static final Logger log = LoggerFactory.getLogger(HorizonStreamTestSteps.class);

    private String horizonStreamBaseUrl;
    private Response restResponse;

//========================================
// Test Step Definitions
//----------------------------------------

    @Given("horizon stream server base url in environment variable {string}")
    public void horizonStreamServerBaseUrlInEnvironmentVariable(String variableName) {
        String value = System.getenv(variableName);

        horizonStreamBaseUrl = value;

        log.info("HORIZON STREAM BASE URL: {}", horizonStreamBaseUrl);
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
