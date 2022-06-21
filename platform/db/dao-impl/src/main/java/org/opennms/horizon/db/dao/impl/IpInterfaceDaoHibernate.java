package org.opennms.horizon.db.dao.impl;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;

import org.opennms.horizon.db.dao.api.IpInterfaceDao;
import org.opennms.horizon.db.model.OnmsIpInterface;

public class IpInterfaceDaoHibernate  extends AbstractDaoHibernate<OnmsIpInterface, Integer> implements IpInterfaceDao {

    public IpInterfaceDaoHibernate(EntityManagerHolder persistenceContextHolder) {
        super(persistenceContextHolder, OnmsIpInterface.class);
    }

    @Override
    public OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress) {
        // Using CAST of ipAddress to string to avoid the complexity of accurately turning an ipAddress string back into
        //  an InetAddress for JPA to query.
        TypedQuery<OnmsIpInterface> query = getEntityManager().createQuery(
                "SELECT n FROM OnmsIpInterface n WHERE n.node.id=:nodeId AND CAST (n.ipAddress AS string) =:ipAddress", OnmsIpInterface.class);

        query.setParameter("nodeId", nodeId);
        query.setParameter("ipAddress", ipAddress);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public OnmsIpInterface findPrimaryInterfaceByNodeId(Integer nodeId) {
        // FIXME: OOPS: Needs to be revised
        TypedQuery<OnmsIpInterface> query = getEntityManager().createQuery(
                "SELECT n FROM OnmsIpInterface n WHERE n.node.id=:nodeId AND n.snmpPrimary = 'P'", OnmsIpInterface.class);
        query.setParameter("nodeId", nodeId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
