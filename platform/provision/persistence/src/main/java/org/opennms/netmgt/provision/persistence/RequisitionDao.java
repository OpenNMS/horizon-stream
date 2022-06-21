package org.opennms.netmgt.provision.persistence;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.netmgt.provision.persistence.model.HibernateRequisitionEntity;

@Transactional
@Slf4j
public class RequisitionDao extends AbstractDaoHibernate<HibernateRequisitionEntity, String>  {

    public RequisitionDao(EntityManagerHolder entityManagerHolder) {
        super(entityManagerHolder, HibernateRequisitionEntity.class);
    }
}
