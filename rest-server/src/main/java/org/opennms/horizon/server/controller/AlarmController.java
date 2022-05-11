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

import javax.servlet.http.HttpServletResponse;

import org.opennms.horizon.server.service.PlatformGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.HttpHeaders;

@RestController
@RequestMapping("/alarms")
public class AlarmController extends AbstractPlatformController {

    public AlarmController(PlatformGateway gateway) {
        super(gateway);
    }



    @GetMapping
    public ResponseEntity<String> listAlarms(HttpServletResponse response, @RequestHeader("Authorization") String authToken) {
        response.setHeader(HttpHeaders.REFERRER_POLICY, "unsafe-url");
        response.setHeader(HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY, "same-site | same-origin | cross-origin");
        return get(PlatformGateway.URL_PATH_ALARMS_LIST, authToken);
    }

    //TODO need to test
    @PutMapping("{id}")
    public ResponseEntity clearAlarmById(@PathVariable Long id, @RequestHeader("Authorization") String authToken, JsonNode data) {
        return put(PlatformGateway.URL_PATH_ALARMS + "/" + id, authToken, data);
    }

    @PutMapping
    public ResponseEntity clearAlarms(@RequestHeader("Authorization") String authToken, JsonNode data) {
        return put(PlatformGateway.URL_PATH_ALARMS, authToken, data);
    }
}
