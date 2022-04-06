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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.opennms.horizon.server.dao.NodeRepository;
import org.opennms.horizon.server.model.dto.NodeDto;
import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.model.mapper.NodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class NodeService {
    private final NodeRepository nodeRepo;
    private final NodeMapper mapper;

    public NodeDto createNode(NodeDto dtoData) {
        Node node = mapper.fromDto(dtoData);
        node.setCreateTime(new Date());
        nodeRepo.save(node);
        return mapper.toDto(node);
    }

    public NodeDto findById(int id) {
        Optional<Node> node = nodeRepo.findById(id);
        if(node.isPresent()) {
            return mapper.toDto(node.get());
        }
        return null;
    }

    public List<NodeDto> findAll() {
        return mapper.toDtoList(nodeRepo.findAll());
    }

    public NodeDto update(int id, NodeDto nodeDto) {
        nodeDto.setId(id);
        Optional<Node> optional = nodeRepo.findById(nodeDto.getId());
        if(optional.isPresent()) {
            Node node = optional.get();
            mapper.updateNodeFromDto(nodeDto, node);
            return mapper.toDto(nodeRepo.save(node));
        }
        return null;
    }

    public boolean delete(int id){
        Optional<Node> node = nodeRepo.findById(id);
        if(node.isPresent()) {
            nodeRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
