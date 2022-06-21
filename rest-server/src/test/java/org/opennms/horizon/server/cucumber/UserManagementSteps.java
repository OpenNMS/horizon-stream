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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.common.VerificationException;
import org.opennms.horizon.server.model.dto.ResetPasswordDTO;
import org.opennms.horizon.server.model.dto.UserDTO;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DocStringType;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;

public class UserManagementSteps {
    private APIClientSteps apiClient;
    private UserDTO itTestUser;

    public UserManagementSteps(APIClientSteps apiClient) {
        this.apiClient = apiClient;
    }

    @DocStringType(contentType = "json")
    public UserDTO jsonStringToUserDto(String jsonString) throws JsonProcessingException {
        return apiClient.getMapper().readValue(jsonString, UserDTO.class);
    }

    @Then("User can list users")
    public void userCanListUsers(DataTable paths) {
        Response response = apiClient.getRequest(APIClientSteps.PATH_USERS);
        assertEquals(200, response.statusCode());
        List<List<String>> pathValueList = paths.asLists();
        for(List<String> pathValue: pathValueList) {
            apiClient.verifyWthJsonPath(response, pathValue);
        }
    }

    @Then("User can create a new user")
    public void userCanCreateANewUser(UserDTO userDTO) {
        Response response = apiClient.postRequest(APIClientSteps.PATH_USERS, apiClient.objectToJson(userDTO));
        assertEquals(200, response.statusCode());
        itTestUser = response.as(UserDTO.class);
        assertNotNull(itTestUser.getId());
        assertTrue(itTestUser.getEnabled());
        assertUser(userDTO, itTestUser);
        assertEquals(1, itTestUser.getRequiredActions().size());
        assertTrue(itTestUser.getRequiredActions().contains("UPDATE_PASSWORD"));
    }

    @Then("User can update a user account")
    public void userCanUpdateAUser(UserDTO updateUser) {
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId(),
                apiClient.objectToJson(updateUser));
        assertEquals(200, response.statusCode());
        UserDTO result = response.as(UserDTO.class);
        assertUser(updateUser, result);
    }

    @Then("User can remove roles from user account")
    public void userRemoveRoles() {
        assertEquals(1, itTestUser.getRoles().size());
        itTestUser.setRoles(new ArrayList<>());
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId(),
                apiClient.objectToJson(itTestUser));
        assertEquals(200, response.statusCode());
        UserDTO result = response.as(UserDTO.class);
        assertTrue(result.getRoles().isEmpty());
        assertUser(itTestUser, result);
    }

    @Then("User can assign role {string} to a user")
    public void assignRoleToUser(String role) {
        itTestUser.setRoles(Arrays.asList(role));
        Response response2 = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId(),
                apiClient.objectToJson(itTestUser));
        assertEquals(200, response2.statusCode());
        UserDTO result2 = response2.as(UserDTO.class);
        assertEquals(1, result2.getRoles().size());
        assertTrue(result2.getRoles().contains(role));
    }

    @Then("User can reset password for another user with new password {string}")
    public void userCanResetPasswordForAUser(String newPassword) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword(newPassword);
        dto.setTemporary(false);
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId() + "/password", apiClient.objectToJson(dto));
        assertEquals(204, response.statusCode());
        Response getUserRes = apiClient.getRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId());
        assertEquals(200, getUserRes.statusCode());
        UserDTO user = getUserRes.as(UserDTO.class);
        assertTrue(user.getRequiredActions().isEmpty());
    }

    @Then("User can delete a user")
    public void userCanDeleteAUser() {
        Response response = apiClient.deleteRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId());
        assertEquals(204, response.statusCode());
        assertEquals(2, (int)apiClient.getRequest(APIClientSteps.PATH_USERS).jsonPath().get("$.size()"));
    }

    @Then("User can search users with partial username {string}")
    public void userCanSearchUsers(String userName, DataTable jsonData) {
        Response response = apiClient.getRequest(APIClientSteps.PATH_USERS + "?username=" + userName);
        assertEquals(200, response.statusCode());
        List<List<String>> pathValues = jsonData.asLists();
        for(List<String> pv: pathValues) {
            apiClient.verifyWthJsonPath(response, pv);
        }
        itTestUser = response.jsonPath().getList(".", UserDTO.class).get(0);
    }

    @Then("User can update his own account")
    public void userCanUpdateHisOwnAccount(UserDTO userDTO) throws VerificationException {
        String userId = apiClient.getUserIDFromToken();
        Response updateRes = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + userId,
                apiClient.objectToJson(userDTO));
        assertEquals(200, updateRes.statusCode());
        UserDTO result = updateRes.as(UserDTO.class);
        assertEquals(userId, result.getId());
        assertUser(userDTO, result);
    }

    @Then("User can reset password to {string} for his own account")
    public void userCanResetPasswordForHisOwnAccount(String newPwd) throws VerificationException {
        String userId = apiClient.getUserIDFromToken();
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword(newPwd);
        dto.setTemporary(true);
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + userId + "/password", apiClient.objectToJson(dto));
        assertEquals(204, response.statusCode());
        Response getUserRes = apiClient.getRequest(APIClientSteps.PATH_USERS + "/" + userId);
        assertEquals(200, getUserRes.statusCode());
        UserDTO user = getUserRes.as(UserDTO.class);
        assertEquals(1, user.getRequiredActions().size());
        assertTrue(user.getRequiredActions().contains("UPDATE_PASSWORD"));
    }

    @Then("User can't change roles for his account")
    public void userNotAllowedToChangeSelfRoles(UserDTO updateDto) throws VerificationException {
        String userId = apiClient.getUserIDFromToken();
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + userId, apiClient.objectToJson(updateDto));
        assertEquals(400, response.statusCode());
    }



    @Then("User can't delete his own account")
    public void userCanTDeleteHisOwnAccount() throws VerificationException {
        String userId = apiClient.getUserIDFromToken();
        Response response = apiClient.deleteRequest(APIClientSteps.PATH_USERS + "/" +userId);
        assertEquals(400, response.statusCode());
    }

    @Then("User can not search users")
    public void userCanNotSearchUsers() {
        Response response = apiClient.getRequest(APIClientSteps.PATH_USERS);
        assertEquals(403, response.statusCode());
    }

    @Then("User can not create user")
    public void userCanNotCreateUser() {
        UserDTO dto = new UserDTO();
        Response response = apiClient.postRequest(APIClientSteps.PATH_USERS, apiClient.objectToJson(dto));
        assertEquals(403, response.statusCode());

    }

    @Then("User can get own account by id")
    public void userCanGetOwnAccountById(DataTable testData) throws VerificationException {
        String userId = apiClient.getUserIDFromToken();
        Response response = apiClient.getRequest(APIClientSteps.PATH_USERS + "/" + userId);
        assertEquals(200, response.statusCode());
        testData.asLists().forEach(pv -> apiClient.verifyWthJsonPath(response, pv));
    }

    @Then("User can get other user account by ID")
    public void userCanGetOtherUserAccountByID(DataTable testData) {
        Response response = apiClient.getRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId());
        assertEquals(200, response.statusCode());
        testData.asLists().forEach(pv -> apiClient.verifyWthJsonPath(response, pv));

    }

    @Then("User can not delete a user")
    public void userCanNotDeleteAUser() {
        Response response = apiClient.deleteRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId());
        assertEquals(403, response.statusCode());
    }

    @Then("User can not update other user account")
    public void userCanNotUpdateOtherUserAccount() {
        Response response = apiClient.putRequest(APIClientSteps.PATH_USERS + "/" + itTestUser.getId(),
                apiClient.objectToJson(itTestUser));
        assertEquals(400, response.statusCode());
    }

    @Then("User can not reset password for other user account")
    public void userCanNotResetPasswordForOtherUserAccount() {
    }
    private void assertUser(UserDTO expected, UserDTO actual) {
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertArrayEquals(expected.getRoles().stream().sorted().toArray(), actual.getRoles().stream().sorted().toArray());
    }


}
