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

package org.opennms.horizon.server.controller;

import org.opennms.horizon.server.service.PlatformGateway;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

public class AbstractPlatformController {
    private final PlatformGateway gateway;

    protected AbstractPlatformController(PlatformGateway gateway) {
        this.gateway = gateway;
    }

    protected ResponseEntity post(String path, String authToken, String data){
        if(gateway.post(path, authToken, data)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    protected ResponseEntity<String> get(String path, String authToken) {
        String result = gateway.get(path, authToken);
        if(result != null){
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }

    protected ResponseEntity put(String path, String authToken, JsonNode data) {
        if(gateway.put(path, authToken, data)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }

    protected ResponseEntity delete(String path, String authToken) {
        if(gateway.delete(path, authToken)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
