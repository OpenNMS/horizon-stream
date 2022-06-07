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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Set;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class KeycloakRoleProviderTest {
    @MockBean
    private KeyCloakUtils mockUtils;

    @Autowired
    private KeycloakConfig config;
    private KeycloakRoleProvider roleProvider;
    private String userID = "test-user-id";
    private Set<String> roles;

    @BeforeEach
    public void setup(){
        roleProvider = (KeycloakRoleProvider) config.initialRoleProvider(mockUtils);
        roles = new HashSet<>();
        roles.add("admin");
        roles.add("user");
    }

    @Test
    public void testLookupUserRoles() {
        doReturn(roles).when(mockUtils).listUserRoles(userID);
        Set<String> result = roleProvider.lookupUserRoles(userID);
        assertEquals(roles, result);
        //The second lookup shouldn't hit the KeycloakUtils
        Set<String> result2 = roleProvider.lookupUserRoles(userID);
        assertEquals(roles, result2);
        verify(mockUtils).listUserRoles(userID);
        verifyNoMoreInteractions(mockUtils);
    }
}
