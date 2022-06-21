package org.opennms.horizon.repository.api;

import org.opennms.horizon.db.model.OnmsMonitoringLocation;
import org.opennms.horizon.db.model.OnmsNode;

public interface NodeRepository  extends BasicCRUD<OnmsNode, Integer> {
    OnmsMonitoringLocation get(String location);
    String saveMonitoringLocation(OnmsMonitoringLocation onmsMonitoringLocation);
}
