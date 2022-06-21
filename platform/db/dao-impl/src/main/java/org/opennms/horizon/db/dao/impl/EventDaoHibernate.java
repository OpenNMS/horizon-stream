package org.opennms.horizon.db.dao.impl;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.opennms.horizon.db.dao.api.EventDao;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.model.OnmsEvent;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;

@Transactional
public class EventDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer> implements EventDao {

    public EventDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsEvent.class);
    }

    @Override
    public int deletePreviousEventsForAlarm(Integer id, OnmsEvent e) {
        Query query = getEntityManager().createQuery("DELETE FROM OnmsEvent e WHERE e.alarm.id=:alarmid AND e.eventId=:eventid");
        query.setParameter("alarmid", id);
        query.setParameter("eventid", id);
        return query.executeUpdate();
    }

}
