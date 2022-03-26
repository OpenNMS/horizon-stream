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

package org.opennms.quarkus;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.quarkus.model.dto.EmployeeDto;
import org.opennms.quarkus.model.entity.Department;
import org.opennms.quarkus.service.EmployeeService;

@Path("/employees")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    @Inject
    EmployeeService empService;

    @GET
    @Path("/{id}")
    public EmployeeDto getEmployee(@PathParam(value = "id") Long id) {
        return empService.getEmployee(id);
    }

    @GET
    @Path("/queryParam")
    public EmployeeDto getEmployeeById(@QueryParam(value = "id") Long id) {
        return empService.getEmployee(id);
    }

    @GET
    public List<EmployeeDto> getAllEmployees() {
        return empService.getAllEmployees();
    }

    @GET
    @Path("/dept/{id}")
    public List<EmployeeDto> getEmployeesByDepartment(@PathParam(value = "id") Long departmentId) {
        return empService.getEmployeesByDepartment(departmentId);
    }

    @GET
    @Path("/search/{name}")
    public List<EmployeeDto> searchEmpsByName(@PathParam(value = "name") String name) {

        return empService.searchEmpsByName(name);
    }

    @POST
    public EmployeeDto createEmployee(EmployeeDto employee) {
        return empService.createEmployee(employee);
    }

    @PUT
    @Path("/{id}")
    public EmployeeDto updateEmployee(@PathParam(value = "id") Long id, EmployeeDto employee) {

        if (employee.getFirst_name() == null || employee.getLast_name() == null) {
            throw new WebApplicationException("first_name or last_name was not set on request.", 422);
        }
        return empService.updateEmployee(id, employee);
    }

    @PUT
    public EmployeeDto updateEmployee(EmployeeDto employee) {

        if (employee.getFirst_name() == null || employee.getLast_name() == null) {
            throw new WebApplicationException("first_name or last_name was not set on request.", 422);
        }
        return empService.updateEmployee(employee);
    }

    @PATCH
    @Path("/{id}/updateDept")
    public EmployeeDto updateEmpDepartmentById(@PathParam(value = "id") Long emp_id , Department department) {

        return  empService.updateEmpDepartment(emp_id,department);
    }

    @PATCH
    @Path("/{empId}/dept/{deptId}")
    public EmployeeDto updateEmpDepartment(@PathParam(value = "empId") Long emp_id ,@PathParam(value = "deptId") Long dept_id ) {

        return  empService.updateEmpDepartment(emp_id,dept_id);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteEmployee(@PathParam(value = "id") Long id) {
        if(empService.deleteEmployee(id)){
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


}
