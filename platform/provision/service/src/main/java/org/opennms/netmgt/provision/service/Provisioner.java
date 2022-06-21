package org.opennms.netmgt.provision.service;

import java.util.List;
import java.util.Optional;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;

public interface Provisioner {
    public static final String ERROR = "error";

    String publish(final RequisitionDTO requisition) throws Exception;
    Optional<RequisitionDTO> read(String name);
    void delete(String name);

    String update(final RequisitionDTO requisitionDTO) throws Exception;

    List<RequisitionDTO> read();

    void performNodeScan();
}
