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

package org.opennms.horizon.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.server.dao.NodeRepository;
import org.opennms.horizon.server.model.dto.NodeDto;
import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.model.mapper.NodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class NodeServiceTest {
    @Autowired
    private NodeService nodeService;
    @Autowired
    private NodeMapper mapper;
    @MockBean
    private NodeRepository nodeRepo;
    private NodeDto nodeDto;
    private int nodeId = 1;

    @BeforeEach
    public void setUP() {
        nodeDto = new NodeDto();
    }

    @Test
    public void testCreateNode() {
        Node node = new Node();
        node.setLabel("test label");
        doReturn(node).when(nodeRepo).save(any(Node.class));
        NodeDto result = nodeService.createNode(nodeDto);
        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo(nodeDto.getLabel());
        assertThat(result.getCreateTime()).isNotNull();
        verify(nodeRepo).findById(anyInt());
        verify(nodeRepo).save(any(Node.class));
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    public void testFindAll() {
        Node node = new Node();
        node.setLabel("test label");
        doReturn(Arrays.asList(node)).when(nodeRepo).findAll();
        List<NodeDto> result = nodeService.findAll();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLabel()).isEqualTo(node.getLabel());
        verify(nodeRepo).findAll();
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    public void testFindById() {
        Node node = new Node();
        node.setId(nodeId);
        node.setLabel("test label");
        doReturn(Optional.of(node)).when(nodeRepo).findById(nodeId);
        NodeDto result = nodeService.findById(nodeId);
        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo(node.getLabel());
        assertThat(result.getId()).isEqualTo(nodeId);
        verify(nodeRepo).findById(nodeId);
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    @DisplayName("update node exist")
    public void testUpdate() {
        Node node = new Node();
        node.setLabel("test label");
        doReturn(Optional.of(node)).when(nodeRepo).findById(nodeId);
        doReturn(node).when(nodeRepo).save(node);
        NodeDto nodeDto = new NodeDto();
        nodeDto.setForeignSource("test foreign resource");
        nodeDto.setLabel("new label");
        NodeDto result = nodeService.update(nodeId, nodeDto);
        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo(nodeDto.getLabel());
        assertThat(result.getForeignSource()).isEqualTo(nodeDto.getForeignSource());
        verify(nodeRepo).findById(nodeId);
        verify(nodeRepo).save(node);
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    @DisplayName("update node doesn't exist")
    public void testUpdateNotFound() {
        doReturn(Optional.empty()).when(nodeRepo).findById(nodeId);
        NodeDto nodeDto = new NodeDto();
        NodeDto result = nodeService.update(nodeId, nodeDto);
        assertThat(result).isNull();
        verify(nodeRepo).findById(nodeId);
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    @DisplayName("test delete node exist")
    public void testDelete() {
        Node node = new Node();
        doReturn(Optional.of(node)).when(nodeRepo).findById(nodeId);
        boolean deleted = nodeService.delete(nodeId);
        assertThat(deleted).isEqualTo(true);
        verify(nodeRepo).findById(nodeId);
        verify(nodeRepo).deleteById(nodeId);
        verifyNoMoreInteractions(nodeRepo);
    }

    @Test
    @DisplayName("test delete node doesn't exist")
    public void testDeleteNotFound() {
        doReturn(Optional.empty()).when(nodeRepo).findById(nodeId);
        boolean deleted = nodeService.delete(nodeId);
        assertThat(deleted).isEqualTo(false);
        verify(nodeRepo).findById(nodeId);
        verifyNoMoreInteractions(nodeRepo);
    }
}
