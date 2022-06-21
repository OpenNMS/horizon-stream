package org.opennms.netmgt.provision.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.horizon.db.model.PrimaryType;
import org.opennms.horizon.repository.api.NodeRepository;
import org.opennms.netmgt.provision.persistence.model.RequisitionRepository;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionInterfaceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionNodeDTO;
import org.opennms.netmgt.provision.scan.NodeScanner;

@Slf4j
public class ProvisionerImplTest extends CamelTestSupport {

    Provisioner provisioner;
    String requisitionJsonStr = null;
    @Mock
    RequisitionRepository requisitionRepository;
    @Mock
    NodeRepository nodeRepository;
    @Mock
    NodeScanner nodeScanner;

    Gson gson = new Gson();
    RequisitionDTO requisitionDTO;

    @Produce
    protected ProducerTemplate template;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        provisioner = new ProvisionerImpl(requisitionRepository, nodeRepository, nodeScanner);

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("import_dummy_requisition.json");
        if (stream == null) {
            throw new FileNotFoundException("Test file does not exist.");
        }
        requisitionJsonStr = IOUtils.toString(stream, StandardCharsets.UTF_8);
        requisitionDTO = gson.fromJson(requisitionJsonStr, RequisitionDTO.class);
        stream.close();
    }

    @Test
    public void publishRequisition() throws Exception {
        when(requisitionRepository.read(anyString())).thenReturn(null);

        provisioner.publish(requisitionDTO);


        verify(requisitionRepository).read(anyString());
        verify(requisitionRepository).save(isA(RequisitionDTO.class));
        verifyNoMoreInteractions(requisitionRepository);
        verify(nodeRepository, times(2)).save(any());
        verify(nodeRepository, times(2)).get(anyString());
        verify(nodeRepository, times(2)).saveMonitoringLocation(any());
        verifyNoMoreInteractions(nodeRepository);
    }

    @Test
    public void readRequisition() throws Exception {

        provisioner.read("blahId");

        verify(requisitionRepository).read(eq("blahId"));
        verifyNoMoreInteractions(requisitionRepository);
    }

    @Test
    public void updateRequisition() throws Exception {

        provisioner.update(requisitionDTO);

        verify(requisitionRepository).update(isA(RequisitionDTO.class));
        verifyNoMoreInteractions(requisitionRepository);
    }

    @Test
    public void deleteRequisition() throws Exception {

        provisioner.delete("blahId");

        verify(requisitionRepository).delete(eq("blahId"));
        verifyNoMoreInteractions(requisitionRepository);
    }
    @Test
    public void generateJson() {
        RequisitionDTO dto = new RequisitionDTO("blahId");

        for(int j=1;j<3;j++) {
            RequisitionNodeDTO nodeDTO = new RequisitionNodeDTO();
            nodeDTO.setNodeLabel("blahNodeLabel"+j);
            nodeDTO.setForeignId("blahForeignId"+j);
            //TODO: think this should be a constant from somewhere
            nodeDTO.setLocation("Default");
            for (int i = 1; i < 6; i++) {
                RequisitionInterfaceDTO interfaceDTO = new RequisitionInterfaceDTO();
                interfaceDTO.setIpAddr(String.format("192.168.1.%03d", i));
                interfaceDTO.setSnmpPrimary(i == 1 ? PrimaryType.PRIMARY : PrimaryType.SECONDARY);
                interfaceDTO.setManaged(true);
                nodeDTO.putInterface(interfaceDTO);
            }
            dto.putNode(nodeDTO);
        }

        assertNotNull(dto);
        try {
            dto.validate();
            log.info(gson.toJson(dto));
        }
        catch (ValidationException ve) {
            log.error(ve.getMessage());
            fail();
        }
    }

}