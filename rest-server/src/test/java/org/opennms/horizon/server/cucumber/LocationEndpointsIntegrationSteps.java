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

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opennms.horizon.server.cucumber.APIClientSteps.PATH_LOCATIONS;

import java.util.List;
import java.util.Map;

import org.opennms.horizon.server.model.dto.MonitoringLocationDto;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationEndpointsIntegrationSteps {
    APIClientSteps apiClient;
    private MonitoringLocationDto testLocation;

    public LocationEndpointsIntegrationSteps(APIClientSteps apiClient) {
        this.apiClient = apiClient;
    }

    @Then("User can create new locations via REST API")
    public void userCanCreateNewLocationsViaRESTAPI(DataTable locations) {
        for (Map<String, String> location: locations.asMaps()) {
            ObjectNode data = apiClient.createJsonNode();
            data.put("location", location.get("location"));
            data.put("monitoringArea", location.get("monitoringArea"));
            Response response = apiClient.postRequest(PATH_LOCATIONS, data);
            assertEquals(200, response.statusCode());
            MonitoringLocationDto result = response.as(MonitoringLocationDto.class);
            assertEquals(location.get("location"), result.getLocation());
            assertEquals(location.get("monitoringArea"), result.getMonitoringArea());
        }
    }


    @Then("User can list locations")
    public void adminUserCanListLocation(DataTable locations) {
        Response response = apiClient.getRequest(PATH_LOCATIONS);
        assertEquals(200, response.statusCode());
        List<MonitoringLocationDto> result = response.jsonPath().getList(".", MonitoringLocationDto.class);
        List<Map<String, String>> locationList = locations.asMaps();
        assertEquals(locationList.size(), result.size());
        for(int i=0; i<locationList.size(); i++) {
            assertTrue(result.get(i).getId()> 0);
            assertEquals(locationList.get(i).get("location"), result.get(i).getLocation());
            assertEquals(locationList.get(i).get("monitoringArea"), result.get(i).getMonitoringArea());
        }
        testLocation = result.get(result.size() -1 );
    }

    @Then("User can get location by ID")
    public void userCanGetLocationByID() {
        Response response = apiClient.getRequest(PATH_LOCATIONS + "/" + testLocation.getId());
        assertEquals(200, response.statusCode());
        MonitoringLocationDto result = response.as(MonitoringLocationDto.class);
        assertNotNull(result);
        assertEquals(testLocation.getLocation(), result.getLocation());
        assertEquals(testLocation.getMonitoringArea(), result.getMonitoringArea());
    }

    @Then("User can update the location")
    public void userCanUpdateTheLocation() {
        testLocation.setMonitoringArea("updated_network");
        Response response = apiClient.putRequest(PATH_LOCATIONS + "/" + testLocation.getId(), apiClient.objectToJson(testLocation));
        assertEquals(200, response.statusCode());
        assertEquals(testLocation.getMonitoringArea(), response.jsonPath().get("monitoringArea"));
    }

    @Then("User can delete the location by ID")
    public void userCanDeleteTheLocationByID() {
        Response response = apiClient.deleteRequest(PATH_LOCATIONS + "/" + testLocation.getId());
        assertEquals(204, response.statusCode());
        List <MonitoringLocationDto> list = apiClient.getRequest(PATH_LOCATIONS).jsonPath().getList(".", MonitoringLocationDto.class);
        assertEquals(1, list.size());
    }

    @Then("User am not allowed to create new location")
    public void UserAmNotAllowedToCreateNewLocation() {
        ObjectNode data = apiClient.createJsonNode();
        data.put("location", "Default");
        data.put("monitoringArea", "localhost");
        Response response = apiClient.postRequest(PATH_LOCATIONS, data);
        assertEquals(403, response.statusCode());
    }

    @Then("User am not allowed to update the location by ID")
    public void userAmNotAllowedToUpdateTheLocationByID() {
        testLocation.setMonitoringArea("updated_network");
        Response response = apiClient.putRequest(PATH_LOCATIONS + "/" + testLocation.getId(), apiClient.objectToJson(testLocation));
        assertEquals(403, response.statusCode());
    }

    @Then("User am not allowed to delete the location")
    public void userAmNotAllowedToDeleteTheLocation() {
        Response response = apiClient.deleteRequest(PATH_LOCATIONS + "/" + testLocation.getId());
        assertEquals(403, response.statusCode());
    }

    @Then("Without correct token user can't access rest api")
    public void withoutInCorrectTokenUserCanTAccessRestApi() {
        apiClient.setAccessToken("Bearer invalid_token");
        Response response = apiClient.getRequest(PATH_LOCATIONS);
        assertEquals(401, response.statusCode());
        apiClient.cleanDB();
    }
}
