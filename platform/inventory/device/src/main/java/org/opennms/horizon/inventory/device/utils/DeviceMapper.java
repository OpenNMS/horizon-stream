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

package org.opennms.horizon.inventory.device.utils;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opennms.horizon.db.dao.api.NodeDao;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.horizon.db.model.OnmsNode;
import org.opennms.horizon.shared.dto.device.DeviceDTO;

@Mapper(componentModel = "spring", uses = {LocationMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class DeviceMapper implements BaseMapper<OnmsNode, DeviceDTO> {
  protected NodeDao nodeDao;
  protected SessionUtils sessionUtils;
  protected LocationMapper locationMapper;

  public void setNodeDao(NodeDao nodeDao) {
    this.nodeDao = nodeDao;
  }

  public void setSessionUtils(SessionUtils sessionUtils) {
    this.sessionUtils = sessionUtils;
  }

  @Mapping(target = "parent", expression = "java(parentIDToNode(dto.getParentId()))")
  @Override
  public abstract OnmsNode fromDto(DeviceDTO dto);

  @Mapping(target = "parentId", expression = "java(parentNodeToId(node.getParent()))")
  @Override
  public abstract DeviceDTO toDto(OnmsNode node);


  public void updateNodeFromDeviceDto(DeviceDTO dto, OnmsNode entity) {
    updateEntityFromDto(dto, entity);
    entity.setParent(dto.getParentId() != null? parentIDToNode(dto.getParentId()): null);
  }

  public OnmsNode parentIDToNode(Integer parentId) {
    return parentId != null? sessionUtils.withReadOnlyTransaction(() -> nodeDao.get(parentId)) : null;
  }

  public Integer parentNodeToId(OnmsNode parent) {
    return parent != null? parent.getId() : null;
  }
}
