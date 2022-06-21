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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/alarms")
@SecurityRequirement(name = "security_auth")
@Tag(name = "Alarm endpoints", description = "The endpoints to manage alarms generated in the platform core")
public class AlarmController {
  private static final String EXAMPLE_ALARM_LIST = "{\"alarm\":[{\"id\":1,\"uei\":\"uei.opennms.org/alarms/trigger\",\"location\":\"Default\",\"nodeId\":null,\"nodeLabel\":null,\"ipAddress\":null,\"serviceType\":null,\"reductionKey\":\"uei.opennms.org/alarms/trigger:::\",\"type\":1,\"count\":1,\"severity\":\"WARNING\",\"firstEventTime\":1655083646422,\"description\":\"A problem has been triggered...\",\"logMessage\":\"A problem has been triggered on //.\",\"operatorInstructions\":null,\"troubleTicket\":null,\"troubleTicketState\":null,\"troubleTicketLink\":null,\"mouseOverText\":null,\"suppressedUntil\":1655083646422,\"suppressedBy\":null,\"suppressedTime\":1655083646422,\"ackUser\":null,\"ackTime\":null,\"clearKey\":null,\"lastEvent\":{\"id\":1,\"uei\":\"uei.opennms.org/alarms/trigger\",\"label\":\"Alarm: Generic Trigger\",\"time\":1655083646422,\"host\":null,\"source\":\"asn-cli-script\",\"ipAddress\":null,\"snmpHost\":null,\"serviceType\":null,\"snmp\":null,\"parameter\":[],\"createTime\":1654997257065,\"description\":\"A problem has been triggered...\",\"logGroup\":null,\"logMessage\":\"A problem has been triggered on //.\",\"severity\":\"WARNING\",\"pathOutage\":null,\"correlation\":null,\"suppressedCount\":null,\"operatorInstructions\":null,\"autoAction\":null,\"operatorAction\":null,\"operationActionMenuText\":null,\"notification\":null,\"troubleTicket\":null,\"troubleTicketState\":null,\"mouseOverText\":null,\"log\":\"Y\",\"display\":\"Y\",\"ackUser\":null,\"ackTime\":null,\"nodeId\":null,\"nodeLabel\":null,\"ifIndex\":null,\"location\":\"Default\"},\"parameter\":[],\"lastEventTime\":1655083646422,\"applicationDN\":null,\"managedObjectInstance\":null,\"managedObjectType\":null,\"ossPrimaryKey\":null,\"x733AlarmType\":null,\"x733ProbableCause\":0,\"qosAlarmState\":null,\"firstAutomationTime\":null,\"lastAutomationTime\":null,\"ifIndex\":null,\"reductionKeyMemo\":null,\"stickyMemo\":null,\"relatedAlarms\":null,\"affectedNodeCount\":0}],\"count\":1,\"totalCount\":1,\"offset\":0}";
  private final PlatformGateway gateway;


    public AlarmController(PlatformGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(name ="alarm list",
            value = EXAMPLE_ALARM_LIST))),
        @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity<String> listAlarms(@RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        return gateway.get(PlatformGateway.URL_PATH_ALARMS_LIST, authToken);
    }

    @PostMapping("/{id}/ack")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity ackAlarm(@PathVariable Long id, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken, @RequestBody String data) {
        return gateway.post(String.format(PlatformGateway.URL_PATH_ALARMS_ACK, id), authToken, data);
    }

   @DeleteMapping("/{id}/ack")
   @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity unAckAlarm(@PathVariable Long id, @RequestHeader("Authorization") @Parameter(hidden = true) String autToken) {
        return gateway.delete(String.format(PlatformGateway.URL_PATH_ALARMS_ACK, id), autToken);
    }

    @PostMapping("/{id}/clear")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    public ResponseEntity clearAlarm(@PathVariable Long id, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken, @RequestBody String data) {
        return gateway.post(String.format(PlatformGateway.URL_PATH_ALARMS_CLEAR, id), authToken, data);
    }
}
