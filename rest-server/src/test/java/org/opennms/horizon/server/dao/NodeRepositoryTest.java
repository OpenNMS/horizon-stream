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

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.opennms.horizon.server.model.entity.MonitoringLocation;
import org.opennms.horizon.server.model.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DataJpaTest
public class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private MonitoringLocationRepository locationRepository;

    @Test
    public void testEnvironmentIsReady() {
        assertThat(nodeRepository).isNotNull();
    }


    @Test
    public void findAllReturnEmpty() {
        List<Node> nodes = nodeRepository.findAll();
        assertThat(nodes).isEmpty();
    }

    @Test
    public void testSaveNodesInLocation() {
        MonitoringLocation location = findLocation("Default");
        Node node1 = createNode();
        node1.setLocation(location);
        Node node2 = createNode();
        node2.setLocation(location);
        location.addNode(node1);
        location.addNode(node2);
        locationRepository.save(location);
        MonitoringLocation dbLocation = locationRepository.findById("Default").orElse(null);
        assertThat(dbLocation).isNotNull();
        assertThat(dbLocation.getNodes().size()).isEqualTo(2);
    }

    @Test
    public void testCreateNode() {
        MonitoringLocation location = findLocation("Default");
        Node node1 = createNode();
        node1.setLocation(location);
        location.addNode(node1);
        nodeRepository.saveAndFlush(node1);
        Node dbNode1 = nodeRepository.findById(node1.getId()).orElse(null);
        assertThat(dbNode1).isNotNull();
        assertThat(dbNode1.getLabel()).isEqualTo(node1.getLabel());
        assertThat(dbNode1.getLocation().getId()).isEqualTo(location.getId());
        Node node2 = createNode();
        node2.setParent(node1);
        node2.setLocation(location);
        location.addNode(node2);
        nodeRepository.save(node2);
        Node dbNode2 = nodeRepository.findById(node2.getId()).orElse(null);
        assertThat(dbNode2).isEqualTo(node2);
        assertThat(dbNode2.getParent()).isEqualTo(node1);
        List<Node> nodes = nodeRepository.findAll();
        assertThat(nodes.size()).isEqualTo(2);
        MonitoringLocation dbLocation = locationRepository.findById("Default").orElse(null);
        assertThat(dbLocation).isNotNull();
        assertThat(dbLocation.getNodes().size()).isEqualTo(2);
    }

    @Test
    public void testUpdatedNode() {
        MonitoringLocation location = findLocation("Default");
        Node node = createNode();
        String label = node.getLabel();
        node.setLocation(location);
        Node savedNode = nodeRepository.save(node);

        savedNode.setLabel(label + " updated label");
        nodeRepository.save(savedNode);

        Node dbNode = nodeRepository.findById(savedNode.getId()).orElse(null);
        assertThat(dbNode).isEqualTo(savedNode);
        assertThat(dbNode.getLabel()).isEqualTo(label + " updated label");
    }

    @Test
    public void testDeleteNode() {
        MonitoringLocation location = findLocation("Default");
        Node node = createNode();
        node.setLocation(location);
        Node savedNode = nodeRepository.save(node);
        Node dbNode = nodeRepository.findById(savedNode.getId()).orElse(null);
        assertThat(dbNode).isNotNull();
        nodeRepository.deleteById(savedNode.getId());

        assertThat(nodeRepository.findById(savedNode.getId())).isEmpty();

    }



    private Node createNode() {
        Node node = new Node();
        node.setType(Node.NodeType.ACTIVE);
        node.setSysName("testSystem");
        node.setSysDescription("A test System");
        node.setSysContact("testsuer@somewhere.com");
        node.setLabel("Kanata_office");
        node.setLabelSource(Node.NodeLabelSource.ADDRESS);
        node.setNetBiosName("test_net_bios");
        node.setDomainName("test.com");
        node.setOperatingSystem("test os");
        node.setLastPoll(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        node.setForeignSource("Company");
        node.setForeignId(String.valueOf(System.currentTimeMillis()));
        node.setLastEgressFlow(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        node.setLastIngressFlow(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        return node;
    }

    private MonitoringLocation findLocation(String locationID) {
        if(locationRepository.existsById(locationID)) {
            return locationRepository.getById(locationID);
        }
        MonitoringLocation location =  new MonitoringLocation();
        location.setId(locationID);
        return locationRepository.save(location);
    }
}


