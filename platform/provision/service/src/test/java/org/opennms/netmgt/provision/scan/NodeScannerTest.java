package org.opennms.netmgt.provision.scan;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionNodeDTO;
import org.opennms.netmgt.provision.persistence.model.ForeignSourceRepository;

@Slf4j
public class NodeScannerTest extends CamelTestSupport {
    NodeScanner nodeScanner;

    @Mock
    LocationAwareDetectorClient locationAwareDetectorClient;
    @Mock
    ForeignSourceRepository foreignSourceRepository;

    RequisitionNodeDTO requisitionNodeDTO;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setupQuartz();
        MockitoAnnotations.openMocks(this);
        requisitionNodeDTO = new RequisitionNodeDTO();
        requisitionNodeDTO.setLocation("blahLocation");
        requisitionNodeDTO.setNodeLabel("blahLabel");
        requisitionNodeDTO.setForeignId("Default");
        nodeScanner = new NodeScanner(context(), locationAwareDetectorClient, foreignSourceRepository);

        ForeignSourceDTO foreignSourceDTO = new ForeignSourceDTO();
        foreignSourceDTO.setScanInterval(new Duration(6000));

        when(foreignSourceRepository.getForeignSource(anyString())).thenReturn(foreignSourceDTO);
    }

    private void setupQuartz() {

        QuartzComponent quartzComponent = new QuartzComponent(context());
        Map<String, String> p = new HashMap<>();
        p.put("org.quartz.scheduler.instanceName","Scheduler");
        p.put("org.quartz.scheduler.instanceId","AUTO");
        p.put("org.quartz.scheduler.skipUpdateCheck","true");
        p.put("org.quartz.scheduler.jobFactory.class","org.quartz.simpl.SimpleJobFactory");
        p.put("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
        p.put("org.quartz.threadPool.threadCount","10");
        quartzComponent.setProperties(p);

        context().addComponent("customQuartz", quartzComponent);
        context.start();
    }

    @Test
    public void scanNode() throws Exception {
        
        nodeScanner.scanNode(requisitionNodeDTO);

//        context().getRoutes().forEach(route -> log.info("######### ROUTE: "+route.getId()));

        assertTrue(context().getRoutes().stream().filter(route -> route.getId().startsWith("SCHEDULED-SCANNER")).collect(Collectors.toList()).size() ==1 );
    }

    @Test
    public void cronFormatTest() {
        String cron = String.format("0/%d", (long)1);
        assertEquals("0/1", cron);

        cron = String.format("0/%d", (long)10);
        assertEquals("0/10", cron);
    }
}