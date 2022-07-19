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

package org.opennms.horizon.inventory.device.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.opennms.horizon.db.dao.api.MonitoringLocationDao;
import org.opennms.horizon.db.dao.api.NodeDao;
import org.opennms.horizon.db.dao.api.SessionUtils;
import org.opennms.horizon.db.model.OnmsNode;
import org.opennms.horizon.inventory.device.utils.DeviceMapper;
import org.opennms.horizon.inventory.device.utils.DeviceMapperImpl;
import org.opennms.horizon.inventory.device.utils.LocationMapper;
import org.opennms.horizon.inventory.device.utils.LocationMapperImpl;
import org.opennms.horizon.shared.dto.device.DeviceDTO;
import org.opennms.horizon.shared.dto.device.LocationDTO;

public class DeviceServiceTest {
  private DeviceService deviceService;
  private LocationMapper locationMapper;
  private DeviceMapper deviceMapper;
  private NodeDao mockNodeDao;
  private MonitoringLocationDao mockLocationDao;

  @Before
  public void setup() {
    locationMapper = new LocationMapperImpl();
    deviceMapper = new DeviceMapperImpl(locationMapper);
    mockNodeDao = mock(NodeDao.class);
    mockLocationDao = mock(MonitoringLocationDao.class);
    deviceService = new DeviceService();
    deviceService.setDao(mockNodeDao);
    deviceService.setLocationDao(mockLocationDao);
    deviceService.setMapper(deviceMapper);
    deviceService.setSessionUtils(new MockSessionUtils());
  }

  @Test
  public void testCreateDeviceWithLocation() {
    LocationDTO location = new LocationDTO();
    DeviceDTO device = new DeviceDTO();
    device.setLocation(location);
    doReturn(1).when(mockNodeDao).save(any(OnmsNode.class));
    Integer id = deviceService.createDevice(device);
    assertEquals(1, id.intValue());
    verify(mockNodeDao).save(any(OnmsNode.class));
    verifyNoMoreInteractions(mockNodeDao);
    verifyNoInteractions(mockLocationDao);
  }

  @Test
  public void testCreateDeviceWithDefaultLocation() {
    DeviceDTO device = new DeviceDTO();
    doReturn(1).when(mockNodeDao).save(any(OnmsNode.class));
    Integer id = deviceService.createDevice(device);
    assertEquals(1, id.intValue());
    verify(mockNodeDao).save(any(OnmsNode.class));
    verify(mockLocationDao).getDefaultLocation();
    verifyNoMoreInteractions(mockLocationDao);
    verifyNoMoreInteractions(mockNodeDao);
  }

  private static class MockSessionUtils implements SessionUtils {
    @Override
    public <V> V withTransaction(Supplier<V> supplier) {
      return supplier.get();
    }

    @Override
    public <V> V withReadOnlyTransaction(Supplier<V> supplier) {
      return supplier.get();
    }
  }
}
