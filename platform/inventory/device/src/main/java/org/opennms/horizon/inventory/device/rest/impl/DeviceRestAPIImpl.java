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

package org.opennms.horizon.inventory.device.rest.impl;

import javax.ws.rs.core.Response;

import org.opennms.horizon.inventory.device.rest.api.DeviceRestAPI;
import org.opennms.horizon.inventory.device.service.DeviceService;
import org.opennms.horizon.shared.dto.device.DeviceCollectionDTO;
import org.opennms.horizon.shared.dto.device.DeviceDTO;

public class DeviceRestAPIImpl implements DeviceRestAPI {
  private DeviceService service;

  public void setService(DeviceService service) {
    this.service = service;
  }

  public DeviceDTO getById(Integer id) {
    return service.getById(id);
  }

  public DeviceCollectionDTO findAll() {
    return service.searchDevices();
  }

  @Override
  public Response createDevice(final DeviceDTO device) {
    try {
      return Response.ok(service.createDevice(device)).build();
    } catch (Exception e){
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }
}
