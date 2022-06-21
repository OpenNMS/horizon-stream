package org.opennms.horizon.db.dao.impl;

import org.opennms.horizon.db.dao.api.DistPollerDao;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.model.OnmsDistPoller;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;

public class DistPollerDaoHibernate extends AbstractDaoHibernate<OnmsDistPoller, String> implements DistPollerDao {

    public DistPollerDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsDistPoller.class);
    }

    @Override
    public OnmsDistPoller whoami() {
        // Return the OnmsDistPoller with the default UUID
        return get(DEFAULT_DIST_POLLER_ID);
    }
}
