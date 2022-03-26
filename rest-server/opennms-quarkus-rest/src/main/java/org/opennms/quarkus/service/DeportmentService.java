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

package org.opennms.quarkus.service;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.opennms.quarkus.model.dto.DepartmentDto;
import org.opennms.quarkus.model.entity.Department;

@ApplicationScoped
public class DeportmentService {
    @Inject
    DepartmentMapper departmentMapper;

    public DepartmentDto getDepartment(Long id) {
        Optional<Department> optionalDepartment = Department.findByIdOptional(id);
        Department department = optionalDepartment.orElseThrow(NotFoundException::new);
        return departmentMapper.toDepartmentDto(department);
    }

    public List<DepartmentDto> getAllDepartments() {
        return departmentMapper.toDepartmentDtoList(Department.listAll());
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto department) {

        Department entity = departmentMapper.toDepartmentEntity(department);
        Department.persist(entity);

        if(entity.isPersistent()) {
            Optional<Department> optionalDept = Department.findByIdOptional(entity.id);
            entity = optionalDept.orElseThrow(NotFoundException::new);
            return departmentMapper.toDepartmentDto(entity);
        } else {
            throw new PersistenceException();
        }

    }
}
