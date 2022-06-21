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
import org.springdoc.core.Constants;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/events")
@SecurityRequirement(name = "security_auth")
@Tag(name = "Event endpoints")
public class EventController {
    private static final String EVENT_SAMPLE_POST = "{\"uei\":\"uei.opennms.org/alarms/trigger\",\"time\":\"2022-05-16T14:17:22.000Z\",\"source\":\"asn-cli-script\",\"descr\":\"A problem has been triggered...\",\"creation-time\":\"2022-05-10T14:17:22.000Z\",\"logmsg\":{\"notify\":true,\"dest\":\"A problem has been triggered on //...\"}}";
    private static final String EVENT_SAMPLE_GET = "{\"id\":1,\"uei\":\"uei.opennms.org/alarms/trigger\",\"label\":\"Alarm: Generic Trigger\",\"time\":1655213934545,\"host\":null,\"source\":\"asn-cli-script\",\"ipAddress\":null,\"snmpHost\":null,\"serviceType\":null,\"snmp\":null,\"parameter\":[],\"createTime\":1655127553219,\"description\":\"A problem has been triggered...\",\"logGroup\":null,\"logMessage\":\"A problem has been triggered on //.\",\"severity\":\"WARNING\",\"pathOutage\":null,\"correlation\":null,\"suppressedCount\":null,\"operatorInstructions\":null,\"autoAction\":null,\"operatorAction\":null,\"operationActionMenuText\":null,\"notification\":null,\"troubleTicket\":null,\"troubleTicketState\":null,\"mouseOverText\":null,\"log\":\"Y\",\"display\":\"Y\",\"ackUser\":null,\"ackTime\":null,\"nodeId\":null,\"nodeLabel\":null,\"ifIndex\":null,\"location\":\"Default\"}";
    private final PlatformGateway gateway;
    public EventController(PlatformGateway gateway) {
        this.gateway = gateway;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = Constants.POST_METHOD, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(schema = @Schema(example = EVENT_SAMPLE_POST))
    ),
        responses = {
        @ApiResponse(responseCode = "202", content = @Content),
        @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity create(@RequestBody JsonNode data, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        log.info("Received post event data {}", data);
        return gateway.post(PlatformGateway.URL_PATH_EVENTS, authToken, data.toString());
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = EVENT_SAMPLE_GET)))
    public ResponseEntity<String> getEventById(@PathVariable Long id, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        return gateway.get(PlatformGateway.URL_PATH_EVENTS+"/"+id, authToken);
    }
}
