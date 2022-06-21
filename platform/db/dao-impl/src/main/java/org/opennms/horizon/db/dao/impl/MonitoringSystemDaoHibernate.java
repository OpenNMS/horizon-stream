package org.opennms.horizon.db.dao.impl;

import org.opennms.horizon.db.dao.api.MonitoringSystemDao;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.model.OnmsMonitoringSystem;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;

public class MonitoringSystemDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringSystem, String> implements MonitoringSystemDao {

    public MonitoringSystemDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsMonitoringSystem.class);
    }
}
