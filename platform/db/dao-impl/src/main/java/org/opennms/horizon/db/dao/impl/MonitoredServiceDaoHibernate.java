/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.db.dao.impl;

import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.api.MonitoredServiceDao;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.horizon.db.model.OnmsMonitoredService;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.net.InetAddress;
import java.util.List;

/**
 * <p>MonitoredServiceDaoHibernate class.</p>
 *
 * @author david
 */
public class MonitoredServiceDaoHibernate extends AbstractDaoHibernate<OnmsMonitoredService, Integer> implements MonitoredServiceDao {

    /**
     * <p>Constructor for MonitoredServiceDaoHibernate.</p>
     */
    public MonitoredServiceDaoHibernate(EntityManagerHolder persistenceContextHolder) {
		super(persistenceContextHolder, OnmsMonitoredService.class);
	}

	/** {@inheritDoc} */
    @Override
	public List<OnmsMonitoredService> findByType(String typeName) {
        TypedQuery<OnmsMonitoredService> query =
            getEntityManager().createQuery("SELECT oms FROM OnmsMonitoredService oms WHERE oms.serviceType.name = :typeName", OnmsMonitoredService.class);

        query.setParameter("typeName", typeName);

        return query.getResultList();
	}

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, String svcName) {
        TypedQuery<OnmsMonitoredService> query =
                getEntityManager().createQuery(
                        "SELECT oms FROM OnmsMonitoredService oms " +
                                "WHERE oms.ipInterface.node.id = :nodeId " +
                                "AND oms.ipInterface.ipAddress = :ipAddress " +
                                "AND oms.serviceType.name = :svcName",
                        OnmsMonitoredService.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("ipAddress", ipAddress);
        query.setParameter("svcName", svcName);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, Integer serviceId) {
        TypedQuery<OnmsMonitoredService> query =
                getEntityManager().createQuery(
                        "SELECT oms FROM OnmsMonitoredService oms " +
                                "WHERE oms.ipInterface.node.id = :nodeId " +
                                "AND oms.ipInterface.ipAddress = :ipAddress " +
                                "AND oms.serviceType.id = :serviceId",
                        OnmsMonitoredService.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("ipAddress", ipAddress);
        query.setParameter("serviceId", serviceId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddr, Integer ifIndex, Integer serviceId) {
        TypedQuery<OnmsMonitoredService> query =
                getEntityManager().createQuery(
                        "SELECT oms FROM OnmsMonitoredService oms " +
                                "WHERE oms.ipInterface.node.id = :nodeId " +
                                "AND oms.ipInterface.ipAddress = :ipAddress " +
                                "AND oms.ipInterface.snmpInterface.ifIndex = :ifIndex " +
                                "AND oms.serviceType.id = :serviceId",
                        OnmsMonitoredService.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("ipAddress", ipAddr);
        query.setParameter("ifIndex", ifIndex);
        query.setParameter("serviceId", serviceId);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
