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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opennms.horizon.server.cucumber.APIClientSteps.PATH_LOCATIONS;
import static org.opennms.horizon.server.cucumber.APIClientSteps.PATH_NODS;

import java.util.List;
import java.util.Map;

import org.opennms.horizon.server.model.dto.MonitoringLocationDto;
import org.opennms.horizon.server.model.dto.NodeDto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class NodeIntegrationSteps {
    private APIClientSteps apiClient;
    private MonitoringLocationDto location;
    private NodeDto testNode;

    public NodeIntegrationSteps(APIClientSteps apiClient) {
        this.apiClient = apiClient;
    }

    @Then("Use can create new node")
    public void useCanCreateNewNode(DataTable dataTable) {
        prepareLocation(true);
        Long parentID = null;
        for(Map<String, String> map: dataTable.asMaps()) {
            NodeDto node = createNodeDto();
            node.setLabel(map.get("label"));
            node.setForeignId(map.get("foreignId"));
            node.setSysLocation(map.get("sysLocation"));
            if(parentID != null) { //this is not the first node
                node.setParentId(parentID);
                node.setLocationId(location.getId());
            }
            Response response = apiClient.postRequest(PATH_NODS, apiClient.objectToJson(node));
            assertEquals(200, response.statusCode());
            NodeDto result = response.as(NodeDto.class);
            assertNotNull(result);
            assertEquals(location.getId(), result.getLocationId());
            if(parentID!=null) {
                assertEquals(parentID, result.getParentId());
            }
            assertNodes(node, result);
            parentID = result.getId();
        }
    }

    @Then("User can list nodes")
    public void userCanListNodes(DataTable table) {
        if(location == null) {
            prepareLocation(false);
        }
        List<Map<String, String>> mapList = table.asMaps();
        Response response = apiClient.getRequest(PATH_NODS);
        assertEquals(200, response.statusCode());
        List<NodeDto> result = response.jsonPath().getList(".", NodeDto.class);
        assertEquals(mapList.size(), result.size());
        Long parentID = null;
        for(int i=0; i<mapList.size(); i++) {
            Map<String, String> map = mapList.get(i);
            NodeDto node = result.get(i);
            assertEquals(map.get("label"), node.getLabel());
            assertEquals(map.get("foreignId"), node.getForeignId());
            assertEquals(map.get("sysLocation"), node.getSysLocation());
            assertEquals(location.getId(), node.getLocationId());
            if(i==0) {
                parentID = node.getId();
            } else {
                assertEquals(parentID, node.getParentId());
            }
        }
        testNode = result.get(result.size()-1);
    }

    @Then("User can get node by ID")
    public void userCanGetNodeByID() {
        Response response = apiClient.getRequest(PATH_NODS + "/" + testNode.getId());
        assertEquals(200, response.statusCode());
        NodeDto result = response.as(NodeDto.class);
        assertNotNull(result);
        assertNodes(testNode, result);
    }

    @Then("User can update node")
    public void userCanUpdateNode() {
        String newLabel = "updated_label";
        testNode.setLabel(newLabel);
        Response response = apiClient.putRequest(PATH_NODS + "/" + testNode.getId(), apiClient.objectToJson(testNode));
        assertEquals(200, response.statusCode());
        NodeDto result = response.as(NodeDto.class);
        assertNotNull(result);
        assertNodes(testNode, result);
    }

    @Then("User can delete node")
    public void userCanDeleteANode() {
        Response response = apiClient.deleteRequest(PATH_NODS + "/" + testNode.getId());
        assertEquals(204, response.statusCode());
        List<NodeDto> list = apiClient.getRequest(PATH_NODS).jsonPath().getList(".", NodeDto.class);
        assertEquals(1, list.size());
    }

    @Then("User is not allowed to create new node")
    public void userIsNotAllowedToCreateNewNode() {
        JsonNode tmpNode = apiClient.createJsonNode();
        Response response = apiClient.postRequest(PATH_NODS, tmpNode);
        assertEquals(403, response.statusCode());
    }

    @Then("User is not allowed to update a node")
    public void userIsNotAllowedToUpdateANode() {
        testNode.setLabel("new_label");
        Response response = apiClient.putRequest(PATH_NODS + "/" + testNode.getId(), apiClient.objectToJson(testNode));
        assertEquals(403, response.statusCode());
    }

    @Then("User is not allowed to delete a node")
    public void userIsNotAllowedToDeleteANode() {
        Response response = apiClient.deleteRequest(PATH_NODS + "/" + testNode.getId());
        assertEquals(403, response.statusCode());
    }

    @Then("Without correct token user can't access node endpoint")
    public void withoutInCorrectTokenUserCanTAccessNodeEndpoint() {
        apiClient.setAccessToken("Bearer invalid_token");
        Response response = apiClient.getRequest(PATH_NODS);
        assertEquals(401, response.statusCode());
    }

    private void prepareLocation(boolean createNew ) {
        if(createNew) {
            ObjectNode data = apiClient.createJsonNode();
            data.put("location", "Default");
            data.put("monitoringArea", "localhost");
            Response response = apiClient.postRequest(PATH_LOCATIONS, data);
            assertEquals(200, response.statusCode());
            location = response.as(MonitoringLocationDto.class);
            assertNotNull(location);
        } else {
            Response response = apiClient.getRequest(PATH_LOCATIONS);
            assertEquals(200, response.statusCode());
            location = response.jsonPath().getList(".", MonitoringLocationDto.class).get(0);
        }
    }

    private NodeDto createNodeDto() {
        NodeDto node = new NodeDto();
        node.setType("A");
        node.setSysOid("os_id");
        node.setOperatingSystem("MacBook Pro");
        node.setSysDescription("MacbookPro with M1 Pro processor");
        node.setLabelSource("H");
        node.setNetBiosName("BiosName");
        node.setDomainName("test.com");
        node.setOperatingSystem("Mac OS");
        node.setForeignSource("Opennms");
        return node;
    }

    private void assertNodes(NodeDto expected, NodeDto actual) {
        assertEquals(expected.getLabel(), actual.getLabel());
        assertEquals(expected.getForeignId(), actual.getForeignId());
        assertEquals(expected.getSysLocation(), actual.getSysLocation());
        assertEquals(expected.getType(), expected.getType());
        assertEquals(expected.getSysOid(), actual.getSysOid());
        assertEquals(expected.getOperatingSystem(), actual.getOperatingSystem());
        assertEquals(expected.getSysDescription(), actual.getSysDescription());
        assertEquals(expected.getLabelSource(), actual.getLabelSource());
        assertEquals(expected.getNetBiosName(), actual.getNetBiosName());
        assertEquals(expected.getDomainName(), actual.getDomainName());
        assertEquals(expected.getForeignSource(), actual.getForeignSource());
    }
}
