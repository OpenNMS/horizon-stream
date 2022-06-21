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

import java.util.List;

import org.opennms.horizon.server.dao.MonitoringLocationRepository;
import org.opennms.horizon.server.model.dto.MonitoringLocationDto;
import org.opennms.horizon.server.model.dto.NewLocationDto;
import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.opennms.horizon.server.model.mapper.MonitoringLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Service
@GraphQLApi
@SecurityRequirement(name = "security_auth")
public class MonitoringLocationService extends AbstractService<MonitoringLocation, MonitoringLocationDto, Long> {
    @Autowired
    public MonitoringLocationService(MonitoringLocationRepository repository, MonitoringLocationMapper mapper) {
        super(repository, mapper);
    }

    @GraphQLQuery(name = "getLocationById")
    public MonitoringLocationDto getLocationById(@GraphQLArgument(name = "id") Long id) {
        return super.findById(id);
    }

    @GraphQLQuery(name = "getAllLocations")
    public List<MonitoringLocationDto> getAllLocations() {
        return super.findAll();
    }

    @PreAuthorize("hasRole('admin')")
    @GraphQLMutation(name = "addLocation")
    public MonitoringLocationDto addLocation(@GraphQLArgument(name = "input") NewLocationDto location) {
        return super.create(location);
    }

    @PreAuthorize("hasRole('admin')")
    @GraphQLMutation(name = "updateLocation")
    public MonitoringLocationDto updateLocation(@GraphQLArgument(name = "id") Long id, @GraphQLArgument(name = "input") MonitoringLocationDto location) {
        return super.update(id, location);
    }

    @PreAuthorize("hasRole('admin')")
    @GraphQLMutation
    public boolean deleteLocation(Long id) {
        return super.delete(id);
    }
}
