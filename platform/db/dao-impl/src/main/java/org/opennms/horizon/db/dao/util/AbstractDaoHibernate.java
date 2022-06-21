package org.opennms.horizon.db.dao.util;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import org.opennms.horizon.db.dao.api.OnmsDao;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;

public abstract class AbstractDaoHibernate<T, K extends Serializable> implements OnmsDao<T, K> {

    private final EntityManagerHolder entityManagerHolder;

    private final Class<T> entityClass;

    public AbstractDaoHibernate(EntityManagerHolder entityManagerHolder, Class<T> entityClass) {
        this.entityManagerHolder = entityManagerHolder;
        this.entityClass = Objects.requireNonNull(entityClass);
    }

    @Override
    public void delete(T entity) {
        getEntityManager().remove(entity);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Override
    public void delete(K key) {
        T entity = get(key);
        getEntityManager().remove(entity);
    }

    @SuppressWarnings("unchecked")
    @Transactional(Transactional.TxType.SUPPORTS)
    @Override
    public List<T> findAll() {
        return getEntityManager().createQuery("Select a from " + entityClass.getSimpleName() + " a").getResultList();
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    @Override
    public T get(K id) {
        return getEntityManager().find(entityClass, id);
    }

    @SuppressWarnings("unchecked")
    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public K save(T entity) {
        getEntityManager().persist(entity);
        return (K)(getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity));
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public void update(T entity) {
        getEntityManager().persist(entity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public void saveOrUpdate(T entity) {
        // FIXME: OOPS: Need more here
        getEntityManager().persist(entity);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @SuppressWarnings("unchecked")
    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public List<T> findMatching(CriteriaQuery<?> query) {
        return (List<T>)getEntityManager().createQuery(query).getResultList();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @Override
    public long countAll() {
        return (Long)getEntityManager().createQuery("SELECT COUNT(x) from " + entityClass.getSimpleName() + " x").getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManagerHolder.getEntityManager();
    }
}
