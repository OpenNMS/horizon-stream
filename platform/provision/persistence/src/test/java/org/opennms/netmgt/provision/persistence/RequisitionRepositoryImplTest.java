package org.opennms.netmgt.provision.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.netmgt.provision.persistence.model.HibernateRequisitionEntity;
import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.netmgt.provision.persistence.model.RequisitionRepository;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;

public class RequisitionRepositoryImplTest {

    public static final String BLAH_ID = "blahId";

    private RequisitionRepository requisitionRepository;
    private RequisitionDTO dto ;

    @Mock
    EntityManager entityManager;
    @Mock
    EntityManagerFactory entityManagerFactory;
    @Mock
    PersistenceUnitUtil persistenceUnitUtil;
    HibernateRequisitionEntity hibernateRequisitionEntity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        EntityManagerHolder p = new DummyPersistenceContextHolder();
        RequisitionDao requisitionDAO = new RequisitionDao(p);
        requisitionRepository = new RequisitionRepositoryImpl(requisitionDAO);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUnitUtil);

        dto = new RequisitionDTO(BLAH_ID);
        hibernateRequisitionEntity = new HibernateRequisitionEntity(dto.getId(), dto);
    }

    @Test
    public void save() {
        String id = requisitionRepository.save(dto);
        assertNotNull(id);
    }

    @Test
    public void read() {
        when(entityManager.find(eq(HibernateRequisitionEntity.class), anyString())).thenReturn(hibernateRequisitionEntity);
        RequisitionDTO readDto = requisitionRepository.read(BLAH_ID);
        assertNotNull(readDto);
    }

    @Test
    public void update() {
        String id = requisitionRepository.update(dto);
        assertNotNull(id);
    }

    @Test
    public void delete() {
        requisitionRepository.delete(BLAH_ID);
        assertTrue(true);
    }

    private class DummyPersistenceContextHolder implements EntityManagerHolder {

        @Override
        public EntityManager getEntityManager() {
            return entityManager;
        }
    }
}