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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.horizon.server.exception.UserManagementException;
import org.opennms.horizon.server.model.dto.ResetPasswordDTO;
import org.opennms.horizon.server.model.dto.UserDTO;
import org.opennms.horizon.server.model.dto.UserSearchDTO;
import org.opennms.horizon.server.model.mapper.KeycloakUserMapper;
import org.opennms.horizon.server.security.KeyCloakUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
    private KeyCloakUtils keyCloakUtils;
    private KeycloakUserMapper userMapper;

    public UserService(KeyCloakUtils keyCloakUtils, KeycloakUserMapper userMapper) {
        this.keyCloakUtils = keyCloakUtils;
        this.userMapper = userMapper;
    }

    public List<UserDTO> searchUsers(UserSearchDTO searchDTO) {
        List<UserRepresentation> users = keyCloakUtils.searchUser(searchDTO);
        users.forEach(user-> user.setRealmRoles(new ArrayList<>(keyCloakUtils.listUserRoles(user.getId()))));
        return userMapper.listToDto(users);
    }

    public UserDTO getUserById(String userId, String authToken) throws UserManagementException {
        verifyUserPermission(userId, authToken, null, Action.READ);
        UserRepresentation user = keyCloakUtils.getUserById(userId);
        user.setRealmRoles(new ArrayList<>(keyCloakUtils.listUserRoles(userId)));
        return userMapper.toDto(user);
    }

    public UserDTO createUser(UserDTO userDTO) throws UserManagementException {
        UserRepresentation user = userMapper.fromDto(userDTO);
        String password = keyCloakUtils.createRandomPassword();
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(true);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(Arrays.asList(credential));
        log.info("create user {} with temp password {}", user.getUsername(), password);
        if(userDTO.getEnabled() == null) {
            user.setEnabled(true);
        }
        UserRepresentation createdUser = keyCloakUtils.createUser(user);
        createdUser.setRealmRoles(new ArrayList<>(keyCloakUtils.listUserRoles(createdUser.getId())));
        return userMapper.toDto(createdUser);
    }

    public UserDTO updateUser(String userId, UserDTO userDTO, String authToken) throws UserManagementException {
        verifyUserPermission(userId, authToken, userDTO.getRoles(), Action.UPDATE);
        UserRepresentation user = keyCloakUtils.getUserById(userId);
        if( user != null) {
            userMapper.updateUserFromDto(userDTO, user);
            keyCloakUtils.updateUser(user, userDTO.getRoles() != null);
            user = keyCloakUtils.getUserById(userId);
            user.setRealmRoles(new ArrayList<>(keyCloakUtils.listUserRoles(userId)));
        }
        return userMapper.toDto(user);
    }

    public boolean deleteUser(String usrId, String authToken) throws UserManagementException {
        verifyUserPermission(usrId, authToken, null, Action.DELETE);
        boolean userDeleted = false;
        UserRepresentation user = keyCloakUtils.getUserById(usrId);
        if(user != null) {
            keyCloakUtils.deleteUser(usrId);
            userDeleted = true;
        }
        return userDeleted;
    }

    public boolean resetPassword(String userId, ResetPasswordDTO passwordDto, String authToken) throws UserManagementException {
        verifyUserPermission(userId, authToken, null, Action.RESET_PWD);
        //TODO we might need to use Keycloak password policy
        if(!StringUtils.hasLength(passwordDto.getNewPassword()) || passwordDto.getNewPassword().length() < KeyCloakUtils.PWD_MINI_LEN) {
            throw new UserManagementException(String.format("Invalid password: %s, password must have at least %d characters.", passwordDto.getNewPassword(), KeyCloakUtils.PWD_MINI_LEN));
        }
        UserRepresentation user = keyCloakUtils.getUserById(userId);
        if(user != null) {
            keyCloakUtils.resetPassword(userId, passwordDto);
            return true;
        }
        return false;
    }

    private void verifyUserPermission(String userId, String authToken, List<String> roles, Action action) throws UserManagementException {
        try {
            String loginUserId = keyCloakUtils.getUserIdFromToken(authToken);
            if(!keyCloakUtils.isAdminUser(authToken) && !userId.equals(loginUserId)) {
                throw new UserManagementException("Login user doesn't have permission target user account");
            }
            if(userId.equals(loginUserId)) {
                if(action.equals(Action.UPDATE)) {
                    Set<String> updateRoles = new HashSet<>(roles);
                    Set<String> assignedRoles = keyCloakUtils.listUserRoles(loginUserId);
                    if (!updateRoles.equals(assignedRoles)) {
                        throw new UserManagementException("Users are not allowed to change their own roles.");
                    }
                } else if(action.equals(Action.DELETE)) {
                    throw new UserManagementException("Users are not allowed to delete their own account.");
                }
            }
        } catch (VerificationException e) {
            log.error("Couldn't get user id from token. ", e);
            throw new UserManagementException("Invalid access token");
        }
    }

    private enum Action {
        DELETE, RESET_PWD, UPDATE, READ
    }
}
