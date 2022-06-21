package org.opennms.horizon.db.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.ObjectNotFoundException;
import org.opennms.horizon.db.dao.api.AcknowledgmentDao;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.horizon.db.model.AckAction;
import org.opennms.horizon.db.model.AckType;
import org.opennms.horizon.db.model.Acknowledgeable;
import org.opennms.horizon.db.model.OnmsAcknowledgment;
import org.opennms.horizon.db.model.OnmsAlarm;
import org.opennms.horizon.db.model.OnmsNotification;
import org.opennms.horizon.db.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcknowledgmentDaoHibernate extends AbstractDaoHibernate<OnmsAcknowledgment, Integer> implements AcknowledgmentDao {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(AcknowledgmentDaoHibernate.class);

    private Logger log = DEFAULT_LOGGER;

    public AcknowledgmentDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsAcknowledgment.class);
    }

    public List<Acknowledgeable> findAcknowledgables(OnmsAcknowledgment ack) {
        List<Acknowledgeable> ackables = new ArrayList<>();

        if (ack == null || ack.getAckType() == null) {
            return ackables;
        }

        if (ack.getAckType().equals(AckType.ALARM)) {
            OnmsAlarm alarm = findAlarm(ack);

            try {
                if (alarm != null && alarm.getAckId() != null) {
                    ackables.add(alarm);
                    List<OnmsNotification> notifs = findRelatedNotifications(alarm);

                    if (notifs != null) {
                        for (OnmsNotification notif : notifs) {
                            try {
                                if (notif.getAckId() != null) {
                                    ackables.add(notif);
                                }
                            } catch (ObjectNotFoundException e) {
                                log.warn("found ackables for alarm #{} but ackable was invalid", ack.getRefId(), e);
                            }
                        }
                    }
                }
            } catch (ObjectNotFoundException e) {
                log.warn("unable to find alarm with ID {}", ack.getRefId(), e);
            }
        }

        else if (ack.getAckType().equals(AckType.NOTIFICATION)) {
            OnmsNotification notif = findNotification(ack);

            try {
                if (notif != null && notif.getAckId() != null) {
                    ackables.add(notif);
                    try {
                        if (notif.getEvent() != null) {
                            OnmsAlarm alarm = notif.getEvent().getAlarm();
                            if (alarm != null) {
                                ackables.add(alarm);
                            }
                        }
                    } catch (ObjectNotFoundException e) {
                        log.warn("unable to find alarm for notification #{}", notif.getNotifyId(), e);
                    }
                }
            } catch (ObjectNotFoundException e) {
                log.warn("unable to find notification with ID {}", ack.getRefId(), e);
            }
        }

        return ackables;
    }


    @Override
    public void processAck(OnmsAcknowledgment ack) {
        log.info("processAck: Searching DB for acknowledgables for ack: {}", ack);
        List<Acknowledgeable> ackables = findAcknowledgables(ack);

        if (ackables == null || ackables.size() < 1) {
            log.debug("processAck: No acknowledgables found.");
            throw new IllegalStateException("No acknowlegables in the database for ack: "+ack);
        }

        log.debug("processAck: Found {}. Acknowledging...", ackables.size());

        Iterator<Acknowledgeable> it = ackables.iterator();
        while (it.hasNext()) {
            try {
                Acknowledgeable ackable = it.next();

                boolean isAlarm = ackable instanceof OnmsAlarm;
                // Consumer<OnmsAlarm> callback = null;

                switch (ack.getAckAction()) {
                    case ACKNOWLEDGE:
                        log.debug("processAck: Acknowledging ackable: {}...", ackable);
                        if (isAlarm) {
                            String ackUser = ackable.getAckUser();
                            Date ackTime = ackable.getAckTime();
                            // callback = (alarm) -> alarmEntityNotifier.didAcknowledgeAlarm(alarm, ackUser, ackTime);
                        }
                        ackable.acknowledge(ack.getAckUser());
                        log.debug("processAck: Acknowledged ackable: {}", ackable);
                        break;
                    case UNACKNOWLEDGE:
                        log.debug("processAck: Unacknowledging ackable: {}...", ackable);
                        if (isAlarm) {
                            String ackUser = ackable.getAckUser();
                            Date ackTime = ackable.getAckTime();
                            // callback = (alarm) -> alarmEntityNotifier.didUnacknowledgeAlarm(alarm, ackUser, ackTime);
                        }
                        ackable.unacknowledge(ack.getAckUser());
                        log.debug("processAck: Unacknowledged ackable: {}", ackable);
                        break;
                    case CLEAR:
                        log.debug("processAck: Clearing ackable: {}...", ackable);
                        if (isAlarm) {
                            ((OnmsAlarm) ackable).getRelatedAlarms().forEach(relatedAlarm -> clearRelatedAlarm(relatedAlarm));
                            OnmsSeverity previousSeverity = ackable.getSeverity();
                            // callback = (alarm) -> alarmEntityNotifier.didUpdateAlarmSeverity(alarm, previousSeverity);
                        }
                        ackable.clear(ack.getAckUser());
                        log.debug("processAck: Cleared ackable: {}", ackable);
                        break;
                    case ESCALATE:
                        log.debug("processAck: Escalating ackable: {}...", ackable);
                        if (isAlarm) {
                            OnmsSeverity previousSeverity = ackable.getSeverity();
                            // callback = (alarm) -> alarmEntityNotifier.didUpdateAlarmSeverity(alarm, previousSeverity);
                        }
                        ackable.escalate(ack.getAckUser());
                        log.debug("processAck: Escalated ackable: {}", ackable);
                        break;
                    default:
                        break;
                }

                updateAckable(ackable);
                save(ack);
                flush();

                // if (callback != null) {
                //     callback.accept((OnmsAlarm)ackable);
                // }
            } catch (Throwable t) {
                log.error("processAck: exception while processing: {}; {}", ack, t);
                throw new RuntimeException("acknowledgement processing failed, t");
            }

        }
        log.info("processAck: Found and processed acknowledgables for the acknowledgement: {}", ack);
    }

    public void updateAckable(Acknowledgeable ackable) {
        EntityManager entityManager = getEntityManager();
        entityManager.merge(ackable);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OnmsAcknowledgment> findLatestAcks(Date from) {
        String hqlQuery = "SELECT acks FROM OnmsAcknowledgment acks " +
                "WHERE acks.ackTime = (" +
                "SELECT MAX(filteredAcks.ackTime) " +
                "FROM OnmsAcknowledgment filteredAcks " +
                "WHERE filteredAcks.refId = acks.refId) " +
                "AND acks.id = (" +
                "SELECT MAX(filteredAcks.id) FROM OnmsAcknowledgment filteredAcks " +
                "WHERE filteredAcks.refId = acks.refId) " +
                "AND acks.ackTime >= (:minAckTimeParm)";

        TypedQuery<OnmsAcknowledgment> query = getEntityManager().createQuery(hqlQuery, OnmsAcknowledgment.class);
        query.setParameter("minAckTimeParm", from);
        try {
            return query.getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Optional<OnmsAcknowledgment> findLatestAckForRefId(Integer refId) {
        TypedQuery<OnmsAcknowledgment> query = getEntityManager().createQuery("SELECT a FROM OnmsAcknowledgment  a WHERE a.refId=:refId ORDER BY a.ackTime, a.id DESC ", OnmsAcknowledgment.class);
        query.setParameter("refId", refId);
        try {
            return Optional.of(query.getSingleResult());
        }  catch (NoResultException e) {
            return Optional.empty();
        }
    }

//========================================
// Internals
//----------------------------------------

    private List<OnmsNotification> findRelatedNotifications(OnmsAlarm alarm) {
        String hql = "from OnmsNotification as n where n.event.alarm = ?";

        EntityManager entityManager = getEntityManager();
        //noinspection JpaQlInspection
        Query query = entityManager.createQuery("from OnmsNotification as n where n.event.alarm = ?1");
        query.setParameter(1, alarm);

        return query.getResultList();
    }

    private OnmsAlarm findAlarm(OnmsAcknowledgment ack) {
        try {
            if (ack != null) {
                EntityManager entityManager = getEntityManager();
                return entityManager.find(OnmsAlarm.class, ack.getRefId());
            }
        } catch (Exception e) {
            log.warn("unable to find alarm with ID {}", ack.getRefId(), e);
        }
        return null;
    }

    private OnmsNotification findNotification(OnmsAcknowledgment ack) {
        //      hql = "from OnmsAlarm as alarms where alarms.id = ?";
        //      return findUnique(OnmsAlarm.class, hql, ack.getRefId());
        try {
            if (ack != null) {
                EntityManager entityManager = getEntityManager();
                return entityManager.find(OnmsNotification.class, ack.getRefId());
            }
        } catch (Exception e) {
            log.warn("unable to find notification with ID {}", ack.getRefId(), e);
        }
        return null;
    }

    private void clearRelatedAlarm(OnmsAlarm alarm) {
        OnmsAcknowledgment clear = new OnmsAcknowledgment(alarm);
        clear.setAckAction(AckAction.CLEAR);
        processAck(clear);
    }
}

