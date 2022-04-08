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

package org.opennms.horizon.server.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

@DataJpaTest
public class MonitoringLocationRepositoryTest {
    @Autowired
    private MonitoringLocationRepository repo;
    @Autowired
    private DataSource dataSource;

    private String location1="test location1";
    private String location2="test location2";

    @Test
    public void testCrudNotFound() {
        List<MonitoringLocation> list = repo.findAll();
        assertThat(list).isEmpty();

        Optional<MonitoringLocation> location = repo.findById(location1);
        assertThat(location).isEmpty();
        assertThrows(EmptyResultDataAccessException.class,
                () -> repo.deleteById(location1), "Entity not found");
    }

    @Test
    public void testCrud() {
        //test save and retrieve
        MonitoringLocation first = createLocation(location1, 3, "first");
        repo.save(first);
        MonitoringLocation firstDB = repo.findById(location1).orElse(null);
        assertThat(firstDB).isNotNull();
        assertThat(firstDB.getId()).isEqualTo(location1);
        assertThat(firstDB.getGeolocation()).isEqualTo(first.getGeolocation());
        assertThat(firstDB.getTags().size()).isEqualTo(3);
        firstDB.getTags().forEach(t->assertThat(t).startsWith("first_"));
        MonitoringLocation second = createLocation(location2, 2,  "second");
        repo.save(second);

        MonitoringLocation secondDB = repo.findById(location2).orElse(null);
        assertThat(secondDB.getId()).isEqualTo(location2);
        assertThat(secondDB.getTags().size()).isEqualTo(2);
        //test list
        List<MonitoringLocation> list = repo.findAll();
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0).getId()).isEqualTo(location1);
        assertThat(list.get(1).getId()).isEqualTo(location2);

        //test update
        String newGeoLocation = "spring.jpa.show-sql";
        String newTag ="extra_tag";
        firstDB.setGeolocation(newGeoLocation);
        firstDB.getTags().add(newTag);
        repo.save(firstDB);
        MonitoringLocation updated = repo.findById(location1).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getGeolocation()).isEqualTo(newGeoLocation);
        assertThat(updated.getTags()).contains(newTag);
        repo.deleteById(location1);
        repo.deleteById(location2);
        assertThat(repo.findById(location1)).isEmpty();
        assertThat(repo.findById(location2)).isEmpty();
        assertThat(repo.findAll()).isEmpty();
    }

    private MonitoringLocation createLocation(String locationId, int tagCount, String tagPrefix){
        MonitoringLocation location = new MonitoringLocation();
        location.setId(locationId);
        location.setLongitude(1245.56);
        location.setLatitude(-235.49);
        location.setGeolocation("test geo location");
        location.setPriority(1);
        location.setMonitoringArea("test area");
        List<String> tags = new ArrayList<>();
        for(int i=0; i<tagCount; i++) {
            tags.add(tagPrefix +"_"+i);
        }
        location.setTags(tags);
        return location;
    }
}
