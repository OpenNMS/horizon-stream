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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.opennms.horizon.server.model.mapper.KeycloakUserMapperImpl;
import org.opennms.horizon.server.security.KeyCloakUtils;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeycloakInitialSteps {

    private String keycloakAuthUrl;
    private KeyCloakUtils keyCloakUtils;
    private String keycloakAdminUser;
    private String keycloakAdminPassword;
    private String adminClientId;

    @Given("Keycloak auth server url in system property {string}")
    public void keycloakAuthServerUrlInSystemProperty(String authUrl) {
        this.keycloakAuthUrl = System.getProperty(authUrl);
    }

    @Given("Keycloak admin user {string} with password {string}")
    public void keycloakAdminUserWithPassword(String user, String password) {
        this.keycloakAdminUser = user;
        this.keycloakAdminPassword = password;
    }

    @Given("Keycloak master realm client-id {string}")
    public void keycloakMasterRealmClientId(String clientId) {
        adminClientId = clientId;
    }

    @Then("Initial Keycloak utils")
    public void initialKeycloakAdminClient() {
        log.info("Initial keycloak utils, {}", this);
        initKeycloakUtils();
    }

    @Then("Create Keycloak realm {string}")
    public void createKeycloakRealm(String realm) {
        keyCloakUtils.setAppRealm(realm);
        keyCloakUtils.createRealm(keycloakAuthUrl);
    }

    @Then("Add roles")
    public void addRoles(List<String> roles) {
        keyCloakUtils.addRoles(roles);
    }


    @Then("Add admin user {string} with password {string} and role {string}")
    public void addAdminUserWithPassword(String username, String password, String role) {
        keyCloakUtils.addUser(username, password, role);
    }

    @Then("Add regular user {string} with password {string} and role {string}")
    public void addRegularUserWithPassword(String user, String pwd, String role) {
        keyCloakUtils.addUser(user, pwd, role);
        keyCloakUtils.close();
    }

    private void initKeycloakUtils() {
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
        clientBuilder.connectionPoolSize(5);
        clientBuilder.connectionCheckoutTimeout(5, TimeUnit.MINUTES);
        KeycloakBuilder kb = KeycloakBuilder.builder();
        kb.serverUrl(keycloakAuthUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm("master")
                .clientId(adminClientId)
                .username(keycloakAdminUser)
                .password(keycloakAdminPassword)
                .resteasyClient(clientBuilder.build());
        Keycloak keycloak = kb.build();
      keyCloakUtils = new KeyCloakUtils(keycloak);
    }
}
