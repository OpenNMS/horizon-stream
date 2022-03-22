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

package org.opennms.webapp.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.opennms.webapp.models.User;
import org.opennms.webapp.persistence.UserPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Path("/users")
public class UserResource {

    private UserPersistence userPersistence;

    @Autowired
    public UserResource(UserPersistence userPersistence) {
        this.userPersistence = userPersistence;
    }

    @GET
    @Produces("application/json")
    public List<User> getAllUsers() {
        return (List<User>) userPersistence.findAll();
    }

    @POST
    @Consumes("application/json")
    public ResponseEntity<Object> createUser(User user) throws URISyntaxException
    {
        if(user.getFirstname() == null || user.getLastname() == null) {
            return ResponseEntity.badRequest().body("Please provide all information");
        }
        User newUser = userPersistence.save(user);
        return ResponseEntity.ok().body(newUser);
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getUserById(@PathParam("id") int id) throws URISyntaxException
    {
        Optional<User> optional = userPersistence.findById(id);
        if(!optional.isPresent()) {
            return Response.status(404).build();
        }
        User dbUser = optional.get();
        return Response
                .status(200)
                .entity(dbUser)
                .contentLocation(new URI(dbUser.getUri())).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateUser(@PathParam("id") int id, User user) throws URISyntaxException
    {
        Optional<User> temp = userPersistence.findById(id);
        if(!temp.isPresent()) {
            return Response.status(404).build();
        }
        User dbUser = temp.get();
        dbUser.setFirstname(user.getFirstname());
        dbUser.setLastname(user.getLastname());
        userPersistence.save(dbUser);
        return Response.status(200).entity(dbUser).contentLocation(new URI(dbUser.getUri())).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) throws URISyntaxException {
        Optional<User> user = userPersistence.findById(id);
        if(user.isPresent()) {
            userPersistence.delete(user.get());
            return Response.status(200).build();
        }
        return Response.status(404).build();
    }
}
