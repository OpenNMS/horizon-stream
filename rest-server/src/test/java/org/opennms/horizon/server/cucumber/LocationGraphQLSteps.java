package org.opennms.horizon.server.cucumber;

import static graphql.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opennms.horizon.server.cucumber.APIClientSteps.PATH_GRAPHQL;

import java.util.List;
import java.util.Map;

import org.opennms.horizon.server.model.dto.MonitoringLocationDto;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
public class LocationGraphQLSteps {
    private APIClientSteps apiClient;
    private MonitoringLocationDto testLocation;
    public LocationGraphQLSteps(APIClientSteps apiClient) {
        this.apiClient = apiClient;
    }

    @Then("User can create new locations")
    public void userCanCreateNewLocations(DataTable locationData) {
        String dataTemplate = "mutation {addLocation(input: {location: \"%s\", monitoringArea: \"%s\"}) " +
                "{id  location monitoringArea}}";
        for(Map<String, String> location: locationData.asMaps()) {
            ObjectNode data = apiClient.createJsonNode();
            data.put("query", String.format(dataTemplate, location.get("location"), location.get("monitoringArea")));
            Response response = apiClient.postRequest(PATH_GRAPHQL, data);
            assertEquals(200, response.statusCode());
            MonitoringLocationDto resultDto = response.jsonPath().getObject("data.addLocation", MonitoringLocationDto.class); //generateLocation(response);
            assertTrue(resultDto.getId() > 0);
            assertEquals(location.get("location"), resultDto.getLocation());
            assertEquals(location.get("monitoringArea"), resultDto.getMonitoringArea());
        }
    }

    @Then("User can query locations")
    public void userCanQueryLocations(DataTable expectData) {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", "{getAllLocations {id location monitoringArea}}");
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertEquals(200, response.statusCode());
        List<MonitoringLocationDto> results = response.jsonPath().getList("data.getAllLocations", MonitoringLocationDto.class);
        List<Map<String, String>> locations = expectData.asMaps();
        assertEquals(locations.size(), results.size());
        for(int i=0; i<locations.size(); i++) {
            assertEquals(locations.get(i).get("location"), results.get(i).getLocation());
            assertEquals(locations.get(i).get("monitoringArea"), results.get(i).getMonitoringArea());
        }
        testLocation = results.get(results.size()-1);//last one
    }

    @Then("User can query a location by ID")
    public void userCanQueryALocationByID() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", String.format("{getLocationById(id: %d){id location monitoringArea tags}}", testLocation.getId()));
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertEquals(200, response.statusCode());
        assertEquals(testLocation.getId(), response.jsonPath().getLong("data.getLocationById.id"));
        assertEquals(testLocation.getLocation(), response.jsonPath().getString("data.getLocationById.location"));
        assertEquals(testLocation.getMonitoringArea(), response.jsonPath().getString("data.getLocationById.monitoringArea"));
    }

    @Then("User can update a location")
    public void userCanUpdateALocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", String.format("mutation {updateLocation(input: {location: \"updated-location\"}, id: %d) ", testLocation.getId()) +
                "{id, location, monitoringArea}}");
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertEquals(200, response.statusCode());
        assertEquals(testLocation.getId(), response.jsonPath().getLong("data.updateLocation.id"));
        assertEquals("updated-location", response.jsonPath().getString("data.updateLocation.location"));
        assertEquals(testLocation.getMonitoringArea(), response.jsonPath().getString("data.updateLocation.monitoringArea"));
    }

    @Then("User can delete a location")
    public void userCanDeleteALocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", String.format("mutation {deleteLocation(id: %d)}", testLocation.getId()));
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertEquals(200, response.statusCode());
        assertTrue(response.jsonPath().getBoolean("data.deleteLocation"));
    }

    @Then("User not allowed to create a location")
    public void userNotAllowedToCreateALocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", "mutation {addLocation(input: {location: \"graphql-test\", monitoringArea: \"localhost\"}) " +
                "{id  location monitoringArea}}");
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertTrue(response.jsonPath().getString("errors[0].message").contains("Access is denied"));
    }

    @Then("User not allowed to update a location")
    public void userNotAllowedToUpdateALocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", String.format("mutation {updateLocation(input: {location: \"updated-location\"}, id: %d) ", testLocation.getId()) +
                "{id, location, monitoringArea}}");
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertTrue(response.jsonPath().getString("errors[0].message").contains("Access is denied"));
    }

    @Then("User not allowed to delete a location")
    public void userNotAllowedToDeleteALocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", String.format("mutation {deleteLocation(id: %d)}", testLocation.getId()));
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertTrue(response.jsonPath().getString("errors[0].message").contains("Access is denied"));
    }

    @Then("Without correct token user can't access graphql api")
    public void withoutCorrectTokenUserNotAllowedAccessGraphqlApi() {
        apiClient.setAccessToken("Bearer invalid_token");
        ObjectNode data = apiClient.createJsonNode();
        data.put("query", "{getAllLocations {id location monitoringArea}}");
        Response response = apiClient.postRequest(PATH_GRAPHQL, data);
        assertEquals(401, response.statusCode());
        apiClient.cleanDB();
    }
}
