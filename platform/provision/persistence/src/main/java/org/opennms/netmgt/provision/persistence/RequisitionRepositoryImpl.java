package org.opennms.netmgt.provision.persistence;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.persistence.model.HibernateRequisitionEntity;
import org.opennms.netmgt.provision.persistence.model.RequisitionRepository;

@Transactional
@Slf4j
@AllArgsConstructor
public class RequisitionRepositoryImpl implements RequisitionRepository {
    private final RequisitionDao requisitionDAO;

    @Override
    public String save(RequisitionDTO requisitionDTO) {
        requisitionDTO.validate();
        HibernateRequisitionEntity hibernateRequisitionEntity = new HibernateRequisitionEntity(requisitionDTO.getId(), requisitionDTO);
        requisitionDAO.save(hibernateRequisitionEntity);
        log.info("Requisition {} persisted to database", hibernateRequisitionEntity.getRequisitionName());
        return hibernateRequisitionEntity.getRequisitionName();
    }

    @Override
    //TODO: return Optional<RequisitionDTO> for all sigs
    public RequisitionDTO read(String id) {
        HibernateRequisitionEntity hibernateRequisitionEntity = requisitionDAO.get(id);

        return hibernateRequisitionEntity == null ? null:hibernateRequisitionEntity.getRequisition();
    }

    @Override
    public void delete(String id) {
        requisitionDAO.delete(id);
    }
    
    @Override
    public String update(RequisitionDTO requisitionDTO) {
        HibernateRequisitionEntity hibernateRequisitionEntity = new HibernateRequisitionEntity(requisitionDTO.getId(), requisitionDTO);
        requisitionDAO.saveOrUpdate(hibernateRequisitionEntity);
        log.info("Requisition {} udpated in database", hibernateRequisitionEntity.getRequisitionName());
        return hibernateRequisitionEntity.getRequisitionName();
    }

    @Override
    public List<RequisitionDTO> read() {
        return requisitionDAO.findAll().stream().map(entity -> entity.getRequisition()).collect(Collectors.toList());
    }
}
