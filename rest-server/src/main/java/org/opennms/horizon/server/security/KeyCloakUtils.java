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

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KeyCloakUtils {
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

    public void addUser(String username, String password, String role) {
        UserRepresentation userRp = new UserRepresentation();
        userRp.setUsername(username);
        if(StringUtils.hasLength(password)) {
            CredentialRepresentation cr = new CredentialRepresentation();
            cr.setType(CredentialRepresentation.PASSWORD);
            cr.setValue(password);
            userRp.setCredentials(Arrays.asList(cr));
        }
        userRp.setEnabled(true);
        addUser(userRp, role);
    }

    public boolean addUser(UserRepresentation userRp, String role) {
        RealmResource realmResource = keycloak.realm(appRealm);
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(userRp);
        String userId = CreatedResponseUtil.getCreatedId(response);
        RoleRepresentation roleRp = realmResource.roles().get(role).toRepresentation();
        UserResource userResource = usersResource.get(userId);
        userResource.roles().realmLevel().add(Arrays.asList(roleRp));
        return response.getStatus()==200;
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
}
