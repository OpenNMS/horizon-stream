package org.opennms.netmgt.provision.persistence.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.Getter;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;

@Getter
public class EntityManagerHolderImpl implements EntityManagerHolder {

    @PersistenceContext(unitName = "dao-provision")
    private EntityManager entityManager;
}
