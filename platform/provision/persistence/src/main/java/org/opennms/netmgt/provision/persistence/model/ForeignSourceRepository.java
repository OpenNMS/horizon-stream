package org.opennms.netmgt.provision.persistence.model;

import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;

public interface ForeignSourceRepository {
    ForeignSourceDTO getForeignSource(String id);

}
