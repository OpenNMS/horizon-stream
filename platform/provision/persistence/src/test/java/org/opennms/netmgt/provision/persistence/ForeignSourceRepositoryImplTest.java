package org.opennms.netmgt.provision.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.persistence.model.ForeignSourceRepository;
import org.opennms.netmgt.provision.persistence.model.HibernateForeignSourceEntity;
import org.opennms.netmgt.provision.persistence.model.HibernateRequisitionEntity;

public class ForeignSourceRepositoryImplTest {

    public static final String BLAH_ID = "blahId";

    private ForeignSourceRepository foreignSourceRepository;
    private ForeignSourceDTO dto ;

    @Mock
    EntityManager entityManager;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    PersistenceUnitUtil persistenceUnitUtil;
    @Mock
    Session session;

    HibernateForeignSourceEntity hibernateForeignSourceEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        EntityManagerHolder p = new DummyPersistenceContextHolder();
        ForeignSourceDao foreignSourceDao = new ForeignSourceDao(p);
        foreignSourceRepository = new ForeignSourceRepositoryImpl(foreignSourceDao);

        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUnitUtil);
        doReturn(session).when(entityManager).unwrap(Session.class);
        dto = new ForeignSourceDTO(BLAH_ID);
        hibernateForeignSourceEntity = new HibernateForeignSourceEntity(dto.getName(), dto);
    }

    @Test
    public void save() {
        String id = foreignSourceRepository.save(dto);
        assertNotNull(id);
    }

    @Test
    public void read() {
        when(entityManager.find(eq(HibernateForeignSourceEntity.class), anyString())).thenReturn(hibernateForeignSourceEntity);
        ForeignSourceDTO readDto = foreignSourceRepository.read(BLAH_ID);
        assertNotNull(readDto);
    }

    @Test
    public void update() {
        String id = foreignSourceRepository.update(dto);
        assertNotNull(id);
    }

    @Test
    public void delete() {
        foreignSourceRepository.delete(BLAH_ID);
        assertTrue(true);
    }

    private class DummyPersistenceContextHolder implements EntityManagerHolder {

        @Override
        public EntityManager getEntityManager() {
            return entityManager;
        }
    }
}
