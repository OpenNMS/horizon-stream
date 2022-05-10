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

import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;

@Configuration
public class KeycloakConfig {
    private final String masterRealm = "master";
    @Value("${keycloak.auth-server-url}")
    private String serverURl;
    @Value("${keycloak.realm}")
    private String appRealm;

    //keycloak admin clients properties for role provider
    @Value("${horizon-stream.keycloak.admin.client-id}")
    private String adminClientId;
    @Value("${horizon-stream.keycloak.admin.username}")
    private String adminUsername;
    @Value("${horizon-stream.keycloak.admin.password}")
    private String adminPassword;
    @Value("${horizon-stream.keycloak.admin.client-pool-size}")
    private int adminClientPoolSize;
    @Value("${horizon-stream.keycloak.admin.client-pool-timeout}")
    private int adminClientPoolTimeOut;

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    Keycloak createKeycloak() {
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
        clientBuilder.connectionPoolSize(adminClientPoolSize)
                .connectionCheckoutTimeout(adminClientPoolTimeOut, TimeUnit.MINUTES);
        KeycloakBuilder kcBuilder = KeycloakBuilder.builder();
        kcBuilder.serverUrl(serverURl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm(masterRealm)
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .resteasyClient(clientBuilder.build());
        return kcBuilder.build();

    }

    @Bean
    AuthenticationTrustResolver createResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    @Autowired
    @Bean
    UserRoleProvider initialRoleProvider(Keycloak keycloakk) {
        return new KeycloakRoleProvider(keycloakk, appRealm);
    }

    @Autowired
    @Bean(name = "customExpression")
    CustomMethodSecurityExpression createExpressRoot(AuthenticationTrustResolver resolver, UserRoleProvider roleProvider) {
        CustomMethodSecurityExpression root = new CustomMethodSecurityExpression();
        root.setTrustResolver(resolver);
        root.setRoleProvider(roleProvider);
        return root;
    }
}
