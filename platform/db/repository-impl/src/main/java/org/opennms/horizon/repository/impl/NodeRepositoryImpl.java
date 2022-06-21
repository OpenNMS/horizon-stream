package org.opennms.horizon.repository.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.opennms.horizon.db.dao.api.MonitoringLocationDao;
import org.opennms.horizon.db.dao.api.NodeDao;
import org.opennms.horizon.db.model.OnmsMonitoringLocation;
import org.opennms.horizon.db.model.OnmsNode;
import org.opennms.horizon.repository.api.NodeRepository;
//import org.opennms.horizon.repository.mapper.RequisitionInterfaceMapper;

@RequiredArgsConstructor
public class NodeRepositoryImpl implements NodeRepository {
    private final NodeDao nodeDao;
    private final MonitoringLocationDao monitoringLocationDao;

    @Override
    public OnmsNode read(String id) {
        return nodeDao.get(Integer.parseInt(id));
    }

    @Override
    public void delete(String id) {
       nodeDao.delete(Integer.parseInt(id));
    }

    @Override
    public Integer save(OnmsNode entity) {
       return nodeDao.save(entity);
    }

    @Override
    public void update(OnmsNode entity) {
       nodeDao.update(entity);
    }

    @Override
    public OnmsMonitoringLocation get(String location) {
        return monitoringLocationDao.get(location);
    }

    @Override
    public String saveMonitoringLocation(OnmsMonitoringLocation onmsMonitoringLocation) {
        return monitoringLocationDao.save(onmsMonitoringLocation);
    }
}
