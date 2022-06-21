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

package org.opennms.horizon.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.horizon.server.exception.UserManagementException;
import org.opennms.horizon.server.model.dto.ResetPasswordDTO;
import org.opennms.horizon.server.model.dto.UserDTO;
import org.opennms.horizon.server.model.dto.UserSearchDTO;
import org.opennms.horizon.server.model.mapper.KeycloakUserMapper;
import org.opennms.horizon.server.security.KeyCloakUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
public class UserServiceTest {
    @MockBean
    private KeyCloakUtils mockKeyCloakUtils;
    @SpyBean
    private KeycloakUserMapper mapper;
    @Autowired
    private UserService userService;


    private static String ROLE_ADMIN = "admin";
    private static String ROLE_USER = "user";
    private static String USERNAME = "test-user";
    private static String USER_ID = "unit-test-user-id";
    private static String TEST_AUTH_TOKEN = "eadfasfasrefasdfasdf";
    private UserRepresentation user;
    private UserDTO userDTO;
    private Set<String> testRoles;


    @BeforeEach
    public void setUp() {
        user = new UserRepresentation();
        user.setUsername(USERNAME);
        user.setId(USER_ID);

        userDTO = new UserDTO();
        userDTO.setUsername(USERNAME);
        userDTO.setId(USER_ID);
        testRoles = new HashSet<>(Arrays.asList(ROLE_USER));
    }

    @AfterEach
    public void afterTest() {
        verifyNoMoreInteractions(mockKeyCloakUtils);
    }

    @Test
    public void testSearchUsers() {
        UserSearchDTO searchDTO = new UserSearchDTO();
        doReturn(Arrays.asList(user)).when(mockKeyCloakUtils).searchUser(searchDTO);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        List<UserDTO> result = userService.searchUsers(searchDTO);
        assertEquals(1, result.size());
        assertUser(user, result.get(0), 1);
        verify(mockKeyCloakUtils).searchUser(searchDTO);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testAdminUserGetOwnAccount() throws VerificationException, UserManagementException {
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        UserDTO result = userService.getUserById(USER_ID, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testAdminUserAccessOtherAccount() throws VerificationException, UserManagementException {
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn("another_user_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        UserDTO result = userService.getUserById(USER_ID, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testRegularUserGetOwnAccount() throws VerificationException, UserManagementException {
        doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        UserDTO result = userService.getUserById(USER_ID, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testRegularUserCannotAccessOtherAccount() throws VerificationException {
        UserManagementException exception = assertThrows(UserManagementException.class, () -> {
            doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn("another_user_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            userService.getUserById(USER_ID, TEST_AUTH_TOKEN);
        }, "UserManagementException is expected");
        assertEquals("Login user doesn't have permission target user account", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
    }

    @Test
    public void testCreateUser() throws UserManagementException {
        doReturn(user).when(mapper).fromDto(userDTO);
        String tmpPwd = "tmp-password";
        doReturn(tmpPwd).when(mockKeyCloakUtils).createRandomPassword();
        doReturn(user).when(mockKeyCloakUtils).createUser(user);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        UserDTO result = userService.createUser(userDTO);
        assertTrue(result.getEnabled());
        assertUser(user, result, 1);
        verify(mapper).fromDto(userDTO);
        verify(mockKeyCloakUtils).createRandomPassword();
        verify(mockKeyCloakUtils).createUser(user);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testAdminUserCanUpdateOwnAccount() throws VerificationException, UserManagementException {
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        userDTO.setRoles(new ArrayList<>(testRoles));
        UserDTO result = userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils, times(2)).getUserById(USER_ID);
        verify(mockKeyCloakUtils, times(2)).listUserRoles(USER_ID);
        verify(mockKeyCloakUtils).updateUser(user, true);
    }

    @Test
    public void testAdminUserCanUpdateOtherAccount() throws VerificationException, UserManagementException {
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn("admin_user_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        userDTO.setRoles(Arrays.asList(ROLE_USER, ROLE_ADMIN));
        UserDTO result = userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils, times(2)).getUserById(USER_ID);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
        verify(mockKeyCloakUtils).updateUser(user, true);
    }

    @Test
    public void testAdminUserCannotChangeRolesInOwnAccount() throws VerificationException {
        UserManagementException exception = assertThrows(UserManagementException.class, () ->{
            doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
            userDTO.setRoles(Arrays.asList(ROLE_USER, ROLE_ADMIN));
            userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        }, "UserManagementException is expected");
        assertEquals("Users are not allowed to change their own roles.", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testRegularUserCanUpdateOwnAccount() throws VerificationException, UserManagementException {
        doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
        userDTO.setRoles(new ArrayList<>(testRoles));
        UserDTO result = userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        assertUser(user, result, 1);
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils, times(2)).getUserById(USER_ID);
        verify(mockKeyCloakUtils, times(2)).listUserRoles(USER_ID);
        verify(mockKeyCloakUtils).updateUser(user, true);
    }

    @Test
    public void testRegularUserCannotChangeRolesInOwnAccount() throws VerificationException {
        UserManagementException exception = assertThrows(UserManagementException.class, () ->{
            doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            doReturn(testRoles).when(mockKeyCloakUtils).listUserRoles(USER_ID);
            userDTO.setRoles(Arrays.asList(ROLE_USER, ROLE_ADMIN));
            userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        }, "UserManagementException is expected");
        assertEquals("Users are not allowed to change their own roles.", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).listUserRoles(USER_ID);
    }

    @Test
    public void testRegularUserCannotUpdateOtherAccount() throws VerificationException {
        UserManagementException exception = assertThrows(UserManagementException.class, () -> {
            doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn("another_user_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            userService.updateUser(USER_ID, userDTO, TEST_AUTH_TOKEN);
        }, "UserManagementException is expected");
        assertEquals("Login user doesn't have permission target user account", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
    }

    @Test
    public void testAdminUserResetOwnPassword() throws VerificationException, UserManagementException {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("12345678910");
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doNothing().when(mockKeyCloakUtils).resetPassword(USER_ID, dto);
        assertTrue(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).resetPassword(USER_ID, dto);
    }

    @Test
    public void testAdminUserResetPasswordForOtherAccount() throws VerificationException, UserManagementException {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("12345678910");
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn("admin_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doNothing().when(mockKeyCloakUtils).resetPassword(USER_ID, dto);
        assertTrue(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).resetPassword(USER_ID, dto);
    }

    @Test
    public void testAdminUserResetPasswordAccountNotFound() throws VerificationException, UserManagementException {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("12345678910");
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn("admin_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(null).when(mockKeyCloakUtils).getUserById(USER_ID);
        assertFalse(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
    }

    @Test
    public void testAdminUserResetPasswordInvalidPwd() throws VerificationException, UserManagementException {
        String invalidPwd = "12345";
        UserManagementException exception = assertThrows(UserManagementException.class, () ->{
            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setNewPassword(invalidPwd);
            doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn("admin_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            assertFalse(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        });
        assertEquals(String.format("Invalid password: %s, password must have at least %d characters.", invalidPwd, KeyCloakUtils.PWD_MINI_LEN), exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
    }

    @Test
    public void testRegularUserResetOwnPassword() throws VerificationException, UserManagementException {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setNewPassword("12345678910");
        doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doNothing().when(mockKeyCloakUtils).resetPassword(USER_ID, dto);
        assertTrue(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).resetPassword(USER_ID, dto);
    }

    @Test
    public void testRegularUserCannotResetForOtherAccount() throws VerificationException, UserManagementException {
        String invalidPwd = "12345";
        UserManagementException exception = assertThrows(UserManagementException.class, () ->{
            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setNewPassword(invalidPwd);
            doReturn(false).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn("another_user_id").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            assertFalse(userService.resetPassword(USER_ID, dto, TEST_AUTH_TOKEN));
        });
        assertEquals("Login user doesn't have permission target user account", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
    }

    @Test
    public void testAdminUserCanDeleteOtherAccount() throws VerificationException, UserManagementException {
        doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        doReturn("another_user").when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        doReturn(user).when(mockKeyCloakUtils).getUserById(USER_ID);
        doNothing().when(mockKeyCloakUtils).deleteUser(USER_ID);
        assertTrue(userService.deleteUser(USER_ID, TEST_AUTH_TOKEN));
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserById(USER_ID);
        verify(mockKeyCloakUtils).deleteUser(USER_ID);

    }

    @Test
    public void testAdminUserCanNotDeleteOwnAccount() throws VerificationException, UserManagementException {
        UserManagementException exception = assertThrows(UserManagementException.class, () ->{
            doReturn(true).when(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
            doReturn(USER_ID).when(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
            assertTrue(userService.deleteUser(USER_ID, TEST_AUTH_TOKEN));
        });
        assertEquals("Users are not allowed to delete their own account.", exception.getMessage());
        verify(mockKeyCloakUtils).isAdminUser(TEST_AUTH_TOKEN);
        verify(mockKeyCloakUtils).getUserIdFromToken(TEST_AUTH_TOKEN);
    }

    private void assertUser(UserRepresentation user, UserDTO actual, int roleSize) {
        assertEquals(user.getId(), actual.getId());
        assertEquals(user.getUsername(), actual.getUsername());
        assertEquals(roleSize, actual.getRoles().size());
        assertEquals(user.getRealmRoles().stream().collect(Collectors.toSet()), actual.getRoles().stream().collect(Collectors.toSet()));
    }
}
