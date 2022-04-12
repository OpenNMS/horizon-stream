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

import javax.transaction.Transactional;

import org.opennms.horizon.server.dao.MonitoringLocationRepository;
import org.opennms.horizon.server.dao.NodeRepository;
import org.opennms.horizon.server.model.dto.NodeDto;
import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.model.mapper.NodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import liquibase.pro.packaged.M;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NodeService extends AbstractService<Node, NodeDto, Integer> {

    private final MonitoringLocationRepository locationRepo;

    @Autowired
    public NodeService(NodeRepository repository, NodeMapper mapper, MonitoringLocationRepository locationRepository) {
        super(repository, mapper);
        locationRepo = locationRepository;
    }

    @Override
    public NodeDto create(NodeDto dto) {
        dto.setCreateTime(new Date());
        Node node = mapper.fromDto(dto);
        boolean locationExist = node.getLocation() != null;
        if(!locationExist) {
            MonitoringLocation location = MonitoringLocation.defaultLocation();
            if(StringUtils.hasLength(dto.getLocation())) {
                location.setId(dto.getLocation());
            } else if (locationRepo.existsById(location.getId())){ //check if default location exists
                locationExist = true;
                location = locationRepo.getById(location.getId());
            }
            location.addNode(node);
            node.setLocation(location);
        }
        if(locationExist) {
            return mapper.toDto(repository.save(node));
        } else {
            locationRepo.save(node.getLocation());
            final String foreignId = node.getForeignId();
            final String foreignSource = node.getForeignSource();
            node = node.getLocation().getNodes().stream().filter(n -> n.getForeignId().equals(foreignId) && n.getForeignSource().equals(foreignSource)).findFirst().orElse(null);
            return mapper.toDto(node);
        }
    }
}
