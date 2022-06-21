/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import com.google.gson.Gson;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.db.model.OnmsIpInterface;
import org.opennms.horizon.db.model.OnmsMonitoredService;
import org.opennms.horizon.db.model.OnmsMonitoringLocation;
import org.opennms.horizon.db.model.OnmsNode;
import org.opennms.horizon.repository.api.NodeRepository;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionInterfaceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionMonitoredServiceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionNodeDTO;
import org.opennms.netmgt.provision.persistence.model.RequisitionRepository;
import org.opennms.netmgt.provision.scan.NodeScanner;

@Slf4j
@RequiredArgsConstructor
public class ProvisionerImpl implements Provisioner {

    private final RequisitionRepository requisitionRepository;
    private final NodeRepository nodeRepository;
    private final NodeScanner nodeScanner;

    private Gson gson = new Gson();

    @Override
    public String publish(RequisitionDTO requisition) {
        requisition.validate();

        log.info("Publishing Requisition {}", requisition);
        requisition.getNodes().values().forEach(node -> {
            Integer id = processNode(node);
            node.setId(id);
        });

        RequisitionDTO existingRequisition = requisitionRepository.read(requisition.getId());

        if (existingRequisition != null) {
            log.info("Updating existing requisition");
            return requisitionRepository.update(requisition);
        }
        else {
            return requisitionRepository.save(requisition);
        }
    }

    private Integer processNode(RequisitionNodeDTO nodeDTO) {
        OnmsNode entityNode = new OnmsNode();
        entityNode.setLabel(nodeDTO.getNodeLabel());

        entityNode.setLocation(createLocationIfNecessary(nodeDTO.getLocation()));

        nodeDTO.getInterfaces().values().forEach( reqInterface -> {
            // Create the Interface Entity
            OnmsIpInterface entityInterface = processInterface(reqInterface);
            entityInterface.setNode(entityNode);
            entityNode.getIpInterfaces().add(entityInterface);
        });

        log.info("Publishing Node {}", entityNode);
        return nodeRepository.save(entityNode);
    }

    private OnmsIpInterface processInterface(RequisitionInterfaceDTO reqInterface) {
        OnmsIpInterface entityInterface = new OnmsIpInterface(reqInterface.getIpAddress().getHostAddress());
        entityInterface.setIsManaged(reqInterface.getManaged() ? "M" : "F");
        entityInterface.setSnmpPrimary(reqInterface.getSnmpPrimary().toString());
        // Create all the monitored services
        reqInterface.getMonitoredServices().values().forEach(service -> {
            OnmsMonitoredService monitoredService = processMoniteredService(service);
            entityInterface.addMonitoredService(monitoredService);
        });
        return entityInterface;

    }

    private OnmsMonitoredService processMoniteredService(RequisitionMonitoredServiceDTO monitoredService) {
        OnmsMonitoredService entityMonitoredService = new OnmsMonitoredService();
        
        return entityMonitoredService;
    }

    private OnmsMonitoringLocation createLocationIfNecessary(String locationStr) {
        //TODO: not complete, need to handle default if null, etc
        OnmsMonitoringLocation location = nodeRepository.get(locationStr);

        if ( location == null) {
            location = new OnmsMonitoringLocation();
            location.setLocationName(locationStr);
            location.setMonitoringArea(locationStr);
            nodeRepository.saveMonitoringLocation(location);
        }
        return location;
    }

    @Override
    public Optional<RequisitionDTO> read(String name) {
        return Optional.ofNullable(requisitionRepository.read(name));
    }

    @Override
    public void delete(String name) {
        requisitionRepository.delete(name);
    }

    @Override
    public String update(RequisitionDTO requisitionDTO) throws Exception {
        return requisitionRepository.update(requisitionDTO);
    }

    @Override
    public List<RequisitionDTO> read() {
        return requisitionRepository.read();
    }

    @Override
    public void performNodeScan() {
        List<RequisitionDTO> requisitions = requisitionRepository.read();
        log.info("Found {} requisitions for scanning", requisitions.size());
        requisitions.forEach(req -> {
            log.info("Requisition: {}", req);
            req.getNodes().values().forEach(node -> {
                try {
                    nodeScanner.scanNode(node);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        });
    }
}
