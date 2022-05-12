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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/alarms")
public class AlarmController {
    private final PlatformGateway gateway;

    public AlarmController(PlatformGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping
    public ResponseEntity<String> listAlarms(@RequestHeader("Authorization") String authToken) {
        String result = gateway.get(PlatformGateway.URL_PATH_ALARMS, authToken);
        if(result != null) {
            return ResponseEntity.ok(result);
        } else {
           return ResponseEntity.badRequest().build();
        }
    }

    //TODO clear alarms
    @PutMapping("{id}")
    public ResponseEntity clearAlarmById(@PathVariable Long id, @RequestHeader("Authorization") String authToken, JsonNode data) {
        return put(PlatformGateway.URL_PATH_ALARMS + "/" + id, authToken, data);
    }

    @PutMapping
    public ResponseEntity clearAlarms(@RequestHeader("Authorization") String authToken, JsonNode data) {
        return put(PlatformGateway.URL_PATH_ALARMS, authToken, data);
    }
}
