/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import org.opennms.horizon.db.dao.api.SnmpInterfaceDao;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.horizon.db.model.OnmsSnmpInterface;
import org.springframework.util.Assert;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
public class SnmpInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsSnmpInterface, Integer> implements SnmpInterfaceDao {
    /**
     * <p>Constructor for SnmpInterfaceDaoHibernate.</p>
     */
    public SnmpInterfaceDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsSnmpInterface.class);
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifIndex) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");

        TypedQuery<OnmsSnmpInterface> query =
                getEntityManager().createQuery("SELECT oif FROM OnmsSnmpInterface oif WHERE oif.node.id = :nodeId AND oif.ifIndex = :ifIndex", OnmsSnmpInterface.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("ifIndex", ifIndex);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<OnmsSnmpInterface> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId may not be null");

        TypedQuery<OnmsSnmpInterface> query =
                getEntityManager().createQuery("SELECT oif FROM OnmsSnmpInterface oif WHERE oif.node.id=:nodeId", OnmsSnmpInterface.class);

        query.setParameter("nodeId", nodeId);

        return query.getResultList();
    }

    @Override
    public OnmsSnmpInterface findByForeignKeyAndIfIndex(String foreignSource, String foreignId, Integer ifIndex) {
        Assert.notNull(foreignSource, "foreignSource may not be null");
        Assert.notNull(foreignId, "foreignId may not be null");
        Assert.notNull(ifIndex, "ifIndex may not be null");

        TypedQuery<OnmsSnmpInterface> query =
                getEntityManager().createQuery(
                        "SELECT oif FROM OnmsSnmpInterface oif " +
                        "WHERE oif.node.foreignSource = :foreignSource " +
                                "AND oif.node.foreignId = :foreignId " +
                                "AND oif.node.type = 'A' " +
                                "AND oif.ifIndex = :ifIndex",
                        OnmsSnmpInterface.class);

        query.setParameter("foreignSource", foreignSource);
        query.setParameter("foreignId", foreignId);
        query.setParameter("ifIndex", ifIndex);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public OnmsSnmpInterface findByNodeIdAndDescription(Integer nodeId, String description) {
        Assert.notNull(nodeId, "nodeId may not be null");
        Assert.notNull(description, "description may not be null");

        TypedQuery<OnmsSnmpInterface> query =
                getEntityManager().createQuery(
                        "SELECT oif FROM OnmsSnmpInterface oif " +
                                "WHERE oif.node.id = :nodeId " +
                                "AND ( LOWER(oif.description) = LOWER(:description) " +
                                "      OR LOWER(oif.ifName) = LOWER(:description) )"
                        ,
                        OnmsSnmpInterface.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("description", description);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
