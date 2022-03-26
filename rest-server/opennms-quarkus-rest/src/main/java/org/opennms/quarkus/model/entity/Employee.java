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

package org.opennms.quarkus.model.entity;

import java.time.LocalDate;
import java.util.List;
;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class Employee extends PanacheEntity {

    public String first_name;
    public String last_name;
    public String gender;
    public LocalDate birth_date;
    public LocalDate hire_date;

    @ManyToOne(fetch = FetchType.LAZY)
    public Department department;

    public static List<Employee> findEmployeesByDepartmentId(Long departmentId) {
        return find("department.id", departmentId).list();
    }

    public static List<Employee> searchEmpsByName(String name) {

        // return find("from Employee e WHERE (0 < LOCATE(?1,e.first_name)) ", name).list();
        return find("first_name like CONCAT('%',?1, '%') ", name).list();
    }
}
