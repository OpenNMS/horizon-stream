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

import java.util.List;

import org.opennms.horizon.server.exception.UserManagementException;
import org.opennms.horizon.server.model.dto.ResetPasswordDTO;
import org.opennms.horizon.server.model.dto.UserDTO;
import org.opennms.horizon.server.model.dto.UserSearchDTO;
import org.opennms.horizon.server.service.UserService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "security_auth")
@Tag(name = "User management endpoints")
public class UserController {

    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "search users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
        @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })

    public List<UserDTO> searchUsers(@ParameterObject UserSearchDTO searchDTO) {
        if(searchDTO == null) {
            searchDTO = new UserSearchDTO();
        }
        return service.searchUsers(searchDTO);
    }

    @GetMapping("/{userID}")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
      @ApiResponse(responseCode = "404", content = @Content),
      @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity getUserById(@PathVariable String userID, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        try {
            UserDTO user = service.getUserById(userID, authToken);
            if (user != null) {
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.notFound().build();
        }catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
        @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO newUser = service.createUser(userDTO);
            log.info("New user {} created", newUser);
            return ResponseEntity.ok(newUser);
        } catch (UserManagementException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", content = @Content),
        @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity updateUser(@PathVariable String userId, @RequestBody UserDTO userDTO, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        try{
        UserDTO updatedUser = service.updateUser(userId, userDTO, authToken);
        if(updatedUser!=null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('admin')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content),
        @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)),
        @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity deleteUser(@PathVariable String userId, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        try {
            if (service.deleteUser(userId, authToken)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (UserManagementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "404", content = @Content),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    })
    public ResponseEntity resetPassword(@PathVariable String userId, @RequestBody ResetPasswordDTO passwordDto, @RequestHeader("Authorization") @Parameter(hidden = true) String authToken) {
        try {
            if(service.resetPassword(userId, passwordDto, authToken)){
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (UserManagementException e) {
            log.error("Failed resetting user password", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
