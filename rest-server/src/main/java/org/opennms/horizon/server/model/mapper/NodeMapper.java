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

package org.opennms.horizon.server.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.opennms.horizon.server.dao.MonitoringLocationRepository;
import org.opennms.horizon.server.dao.NodeRepository;
import org.opennms.horizon.server.model.dto.NodeDto;
import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.model.entity.Node.NodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


@Mapper(componentModel = "spring")
public abstract class NodeMapper implements EntityDtoMapper<Node, NodeDto> {
    @Autowired
    protected NodeRepository nodeRepo;
    @Autowired
    protected MonitoringLocationRepository locationRepo;

    @Mapping(target = "parent", expression = "java(nodeRepo.existsById(dto.getParentId())? nodeRepo.getById(dto.getParentId()): null)")
    @Override
    public abstract Node fromDto(NodeDto dto);

    @Mapping(source = "parent.id", target = "parentId")
    @Override
    public abstract NodeDto toDto(Node node);

    String locationToString(MonitoringLocation location) {
        return (location == null)? null: location.getId();
    }

    MonitoringLocation locationIDToLocation(String locationID) {
        if(StringUtils.hasLength(locationID)) {
            return locationRepo.existsById(locationID)? locationRepo.getById(locationID): null;
        }
        return null;
    }

    String typeToString(NodeType type) {
        return (type == null) ? null: type.toString();
    }

    NodeType stringToType(String s){
        return NodeType.fromValueString(s);
    }

    String labelSourceToString(Node.NodeLabelSource labelSource) {
        if(labelSource != null) {
            return labelSource.toString();
        }
        return null;
    }

    Node.NodeLabelSource stringToLabelSource(String s) {
        return Node.NodeLabelSource.fromValueString(s);
    }
}
