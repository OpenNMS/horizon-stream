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

package org.opennms.horizon.server.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.opennms.horizon.server.exception.UserManagementException;
import org.opennms.horizon.server.model.dto.ResetPasswordDTO;
import org.opennms.horizon.server.model.dto.UserSearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import liquibase.repackaged.org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KeyCloakUtils {
    private final String pwdChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
    public static final int PWD_MINI_LEN = 10;
    public static final String USER_ROLE_ADMIN = "admin";
    @Value("${keycloak.realm}")
    private String appRealm;
    private final Keycloak keycloak;

    @Autowired
    public KeyCloakUtils(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void setAppRealm(String appRealm) {
        this.appRealm = appRealm;
    }

    public void createRealm(String frontendUrl) {
        RealmRepresentation realmRp = new RealmRepresentation();
        realmRp.setId(appRealm);
        realmRp.setRealm(appRealm);
        realmRp.setEnabled(true);
        if(StringUtils.hasLength(frontendUrl)) {
            Map<String, String> attr = new HashMap<>();
            attr.put("frontendUrl", frontendUrl);
            realmRp.setAttributes(attr);
        }
        keycloak.realms().create(realmRp);
    }

    public void addRole(String role) {
        RoleRepresentation rr = new RoleRepresentation();
        rr.setName(role);
        rr.setName(role);
        keycloak.realm(appRealm).roles().create(rr);
    }

    public UserRepresentation addUser(String username, String password, String role) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setRealmRoles(Arrays.asList(role));
        user.setEnabled(true);
        try {
            CredentialRepresentation cr = new CredentialRepresentation();
            cr.setType(CredentialRepresentation.PASSWORD);
            cr.setValue(password);
            user.setCredentials(Arrays.asList(cr));
            return createUser(user);
        } catch (UserManagementException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void addRoles(List<String> roles) {
        roles.forEach(r -> addRole(r));
    }

    /**
     *
     * @param usrId the keycloak user id
     * @return set of realm roles assigned to the user
     */
    public Set<String> listUserRoles(String usrId) {
        log.info("list roles for user {} with realm {}", usrId, appRealm);
        UserResource userResource = keycloak.realm(appRealm).users().get(usrId);
        try {
            return userResource.roles().getAll().getRealmMappings().stream().map(r->r.getName()).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("failed list user roles: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    public void close() {
        if(!keycloak.isClosed()) {
            keycloak.close();
        }
    }

    public List<UserRepresentation> searchUser(UserSearchDTO searchDTO) {
        return keycloak.realm(appRealm).users().search(searchDTO.getUsername(), searchDTO.getFirstName(),
                searchDTO.getLastName(), searchDTO.getEmail(), searchDTO.getFirst(), searchDTO.getMax(),
                searchDTO.getEnabled(), null);
    }

    public UserRepresentation getUserById(String userId) {
        try {
            return keycloak.realm(appRealm).users().get(userId).toRepresentation();
        } catch (NotFoundException e){
            return null;
        }
    }

    public String createRandomPassword() {
        return RandomStringUtils.random(PWD_MINI_LEN, pwdChars);
    }

    public UserRepresentation createUser(UserRepresentation user) throws UserManagementException {
        RealmResource realmResource = keycloak.realm(appRealm);
        Response response = realmResource.users().create(user);
        if(response.getStatus()!= Response.Status.CREATED.getStatusCode()) {
            throw new UserManagementException(String.format("Failed on creating new user with username: %s and email: %s. The error is: %s",
                    user.getUsername(), user.getEmail(), ((ClientResponse) response).getReasonPhrase()));
        }
        String userId = CreatedResponseUtil.getCreatedId(response);
        if(user.getRealmRoles()!=null&&user.getRealmRoles().size()>0) {
            assignRoles(realmResource, userId, user.getRealmRoles());
        }
        UserResource userResource = realmResource.users().get(userId);
        UserRepresentation newUser = userResource.toRepresentation();
        return newUser;
    }

    private void assignRoles(RealmResource realmResource, String userId, List<String> roles){
        if(roles !=null) {
            UserResource userResource = realmResource.users().get(userId);
            List<RoleRepresentation> oldRoles = userResource.roles().realmLevel().listAll();
            userResource.roles().realmLevel().remove(oldRoles);
            List<RoleRepresentation> newRoles = roles.stream().map(r->realmResource.roles().get(r).toRepresentation())
                    .collect(Collectors.toList());
            userResource.roles().realmLevel().add(newRoles);
        }
    }

    public UserRepresentation updateUser(UserRepresentation user, boolean updateRoles) {
        RealmResource realmResource = keycloak.realm(appRealm);
        UserResource userRes = realmResource.users().get(user.getId());
        userRes.update(user);
        if(updateRoles) {
            assignRoles(realmResource, user.getId(), user.getRealmRoles());
        }
        UserRepresentation updatedUser = userRes.toRepresentation();
        return updatedUser;
    }

    public void deleteUser(String userId) {
        keycloak.realm(appRealm).users().get(userId).remove();
    }

    public void resetPassword(String userId, ResetPasswordDTO passwordDto) {
        CredentialRepresentation newPwd = new CredentialRepresentation();
        newPwd.setType(CredentialRepresentation.PASSWORD);
        newPwd.setValue(passwordDto.getNewPassword());
        if(passwordDto.getTemporary()!=null) {
            newPwd.setTemporary(passwordDto.getTemporary());
        }
        keycloak.realm(appRealm).users().get(userId).resetPassword(newPwd);
    }

    public  String getUserIdFromToken(String authToken) throws VerificationException {
        return createAccessTokenFromAuthString(authToken).getSubject();
    }

    public boolean isAdminUser(String authToken) throws VerificationException {
        String userId = getUserIdFromToken(authToken);
        return listUserRoles(userId).contains(USER_ROLE_ADMIN);
    }

    private AccessToken createAccessTokenFromAuthString(String authToken) throws VerificationException {
        String accessToken = authToken.startsWith("Bearer")? authToken.substring(7) : authToken;
        return TokenVerifier.create(accessToken, AccessToken.class).getToken();
    }
}
