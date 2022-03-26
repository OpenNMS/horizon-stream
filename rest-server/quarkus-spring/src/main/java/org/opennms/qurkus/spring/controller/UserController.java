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

package org.opennms.qurkus.spring.controller;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.opennms.qurkus.spring.model.User;
import org.opennms.qurkus.spring.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserRepo userRepo;

    @Autowired
    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findUserById(@PathVariable(value = "id") long id) {
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()) {
            return ResponseEntity.ok().body(user.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public User createNewUser(@RequestBody User user) {
        return userRepo.save(user);
    }

    @DeleteMapping("/{id}")
    public Response deleteUser(@PathVariable(value = "id")long id) {
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()) {
            userRepo.deleteById(id);
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
