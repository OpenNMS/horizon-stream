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

package org.micronaut.example.controller;

import static io.micronaut.http.HttpResponse.ok;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.micronaut.example.model.PostDetailDTO;
import org.micronaut.example.model.PostSummeryDTO;
import org.micronaut.example.repo.CommentRepo;
import org.micronaut.example.repo.PostRepo;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@Controller("/posts")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Validated
public class PostController {
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;

    @Get(produces = MediaType.APPLICATION_HAL_JSON)
    public HttpResponse<List<PostSummeryDTO>> getAll() {
        var body = postRepo.findAll()
                .stream().map(p -> new PostSummeryDTO(p.getId(), p.getTitle(), p.getCreatedAt()))
                .collect(Collectors.toList());
        return ok(body);
    }

    @Get(value = "/{id}", produces = MediaType.APPLICATION_HAL_JSON)
    public HttpResponse<PostDetailDTO> findById(@PathVariable UUID id){
        return postRepo.findById(id)
                .map(p -> ok(new PostDetailDTO(p.getId(), p.getTitle(), p.getCreatedAt(), p.getContent())))
                .orElseGet(HttpResponse::notFound);

    }


}
