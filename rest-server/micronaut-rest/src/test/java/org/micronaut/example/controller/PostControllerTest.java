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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.micronaut.example.model.Post;
import org.micronaut.example.model.PostSummeryDTO;
import org.micronaut.example.repo.CommentRepo;
import org.micronaut.example.repo.PostRepo;

import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest(environments = Environment.TEST)
public class PostControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    PostRepo postRepo;

    @Inject
    CommentRepo commentRepo;

    @MockBean(PostRepo.class)
    PostRepo postRepo() {
        return mock(PostRepo.class);
    }

    @MockBean(CommentRepo.class)
    CommentRepo commentRepo(){
        return mock(CommentRepo.class);
    }

    @Test
    @DisplayName("test GET '/posts'")
    public void testGetAll() throws Exception {
        when(postRepo.findAll()).thenReturn(
                List.of(Post.builder().id(UUID.randomUUID()).title("test title").content("test content").build())
        );
        var response = client.toBlocking().exchange("/posts", PostSummeryDTO[].class);
        assertEquals(HttpStatus.OK, response.status());
        var body = response.body();
        assertThat(body.length).isEqualTo(1);
        assertThat(body[0].getTitle()).isEqualTo("test title");
        verify(this.postRepo, times(1)).findAll();
        verifyNoMoreInteractions(this.postRepo);
    }

}
