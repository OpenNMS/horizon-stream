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
import javax.ws.rs.WebApplicationException;

import org.opennms.quarkus.model.dto.EmployeeDto;
import org.opennms.quarkus.model.entity.Department;
import org.opennms.quarkus.model.entity.Employee;

@ApplicationScoped
public class EmployeeService {

    @Inject
    EmployeeMapper employeeMapper;

    public EmployeeDto getEmployee(Long id) {
        return employeeMapper.toEmployeeDto(Employee.findById(id));
    }

    public List<EmployeeDto> getAllEmployees() {
        // return employeeMapper.toEmployeeList(Employee.listAll());
        return employeeMapper.toEmployeeList(Employee.findAll().list());
    }

    public List<EmployeeDto> getEmployeesByDepartment(Long deptId) {
        return employeeMapper.toEmployeeList(Employee.findEmployeesByDepartmentId(deptId));
    }

    public List<EmployeeDto> searchEmpsByName(String name) {
        return employeeMapper.toEmployeeList(Employee.searchEmpsByName(name));
    }

    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employee) {

        Employee entity = employeeMapper.toEmployeeEntity(employee);
        Employee.persist(entity);

        entity.persistAndFlush();
        // either use persistAndFlush or persist method
        // entity.persist();
        if(entity.isPersistent()) {
            Optional<Employee> optionalEmp = Employee.findByIdOptional(entity.id);
            entity = optionalEmp.orElseThrow(NotFoundException::new);
            return employeeMapper.toEmployeeDto(entity);
        } else {
            throw new PersistenceException();
        }

    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employee) {
        Employee entity  = Employee.findById(id);
        if(entity == null) {
            throw new WebApplicationException("Employee with id of " + id + " does not exist.", 404);
        }

        // either use mapping
        employeeMapper.updateEmployeeEntityFromDto(employee,entity);

        // or update the entity with
//        entity.last_name = employee.getLast_name() ;
//        entity.first_name = employee.getFirst_name();
//        entity.birth_date = employee.getBirth_date();
//        entity.hire_date = employee.getHire_date();
//
//        if(entity.department != null && employee.getDepartment().id != null) {
//            entity.department.id = employee.getDepartment().id;
//        }
//
//        if(entity.department == null && employee.getDepartment().id != null) {
//            Department department = new Department();
//            department.id = employee.getDepartment().id;
//            entity.department = department;
//        }

        return employeeMapper.toEmployeeDto(entity);
    }

    @Transactional
    public EmployeeDto updateEmployee(EmployeeDto employee) {
        Employee entity  = Employee.findById(employee.getId());
        if(entity == null) {
            throw new WebApplicationException("Employee with id " + employee.getId() + " does not exist.", 404);
        }
        employeeMapper.updateEmployeeEntityFromDto(employee,entity);
        entity =  Employee.getEntityManager().merge(entity);
        return employeeMapper.toEmployeeDto(entity);
    }

    @Transactional
    public EmployeeDto updateEmpDepartment(Long empId, Department department) {
        Employee entity  = Employee.findById(empId);
        if(entity == null) {
            throw new WebApplicationException("Employee with id " + empId + " does not exist.", 404);
        }
        entity.department = department;

        return employeeMapper.toEmployeeDto(entity);
    }

    @Transactional
    public EmployeeDto updateEmpDepartment(Long empId, Long deptId) {
        Employee entity  = Employee.findById(empId);
        if(entity == null) {
            throw new WebApplicationException("Employee with id " + empId + " does not exist.", 404);
        }

        Optional<Department> department = Department.findByIdOptional(deptId);
        if(department.isPresent()) {
            entity.department = department.get();
        }


        return employeeMapper.toEmployeeDto(entity);
    }


    @Transactional
    public boolean deleteEmployee(Long id) {

        return Employee.deleteById(id);

    }
}

