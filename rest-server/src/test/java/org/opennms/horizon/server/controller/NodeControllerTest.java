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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.server.model.dto.NodeDto;
import org.opennms.horizon.server.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest
@AutoConfigureWebTestClient
public class NodeControllerTest {
    @Autowired
    private WebTestClient webClient;
    @MockBean
    private NodeService nodeService;

    private static final String URL_PATH = "/nodes";
    private final int nodeId = 1;
    private NodeDto node1;
    private NodeDto node2;

    @BeforeEach
    public void setUp() {
        node1 = new NodeDto();
        node1.setId(nodeId);
        node1.setLabel("test label1");

        node2 = new NodeDto();
        node2.setId(nodeId + 1);
        node2.setLabel("test label2");
    }

    @Test
    public void testListAll() {
        doReturn(Arrays.asList(node1, node2)).when(nodeService).findAll();
        webClient.get().uri(URL_PATH)
                .exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.size()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].label").isEqualTo(node1.getLabel())
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].label").isEqualTo(node2.getLabel());
        verify(nodeService).findAll();
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testFindById() {
        doReturn(node1).when(nodeService).findById(nodeId);

        webClient.get().uri(URL_PATH + "/" + nodeId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(nodeId)
                .jsonPath("$.label").isEqualTo(node1.getLabel());
        verify(nodeService).findById(nodeId);
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testFindByIdNotFound() {
        doReturn(null).when(nodeService).findById(nodeId);
        webClient.get().uri(URL_PATH+ "/" + nodeId)
                .exchange()
                .expectStatus().isNotFound();
        verify(nodeService).findById(nodeId);
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testCreat() {
        NodeDto nodeDto = new NodeDto();
        doReturn(node1).when(nodeService).create(any(NodeDto.class));
        webClient.post().uri(URL_PATH).bodyValue(nodeDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(nodeId)
                .jsonPath("$.label").isEqualTo(node1.getLabel());
        verify(nodeService).create(any(NodeDto.class));
    }

    @Test
    public void testUpdate() {
        NodeDto nodeDto = new NodeDto();
        doReturn(node1).when(nodeService).update(eq(nodeId), any(NodeDto.class));
        webClient.put().uri(URL_PATH + "/" + nodeId).bodyValue(nodeDto).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(nodeId)
                .jsonPath("$.label").isEqualTo(node1.getLabel());
        verify(nodeService).update(eq(nodeId),any(NodeDto.class));
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testUpdateNotFound() {
        NodeDto nodeDto = new NodeDto();
        doReturn(null).when(nodeService).update(eq(nodeId), any(NodeDto.class));
        webClient.put().uri(URL_PATH + "/" + nodeId).bodyValue(nodeDto).exchange()
                        .expectStatus().isNotFound();
        verify(nodeService).update(eq(nodeId), any(NodeDto.class));
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testDelete() {
        doReturn(true).when(nodeService).delete(nodeId);
        webClient.delete().uri(URL_PATH + "/" + nodeId).exchange()
                .expectStatus().isNoContent();
        verify(nodeService).delete(nodeId);
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void testDeleteNoFound() {
        doReturn(false).when(nodeService).delete(nodeId);
        webClient.delete().uri(URL_PATH + "/" + nodeId).exchange()
                .expectStatus().isNotFound();
        verify(nodeService).delete(nodeId);
        verifyNoMoreInteractions(nodeService);
    }
}
