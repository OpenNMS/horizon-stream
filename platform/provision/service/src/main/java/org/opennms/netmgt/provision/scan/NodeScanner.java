package org.opennms.netmgt.provision.scan;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.opennms.horizon.db.model.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.LocationAwareDetectorClient;
import org.opennms.netmgt.provision.detector.DetectorRunner;
import org.opennms.netmgt.provision.persistence.dto.ForeignSourceDTO;
import org.opennms.netmgt.provision.persistence.dto.PluginConfigDTO;
import org.opennms.netmgt.provision.persistence.dto.RequisitionNodeDTO;
import org.opennms.netmgt.provision.persistence.model.ForeignSourceRepository;
import org.opennms.netmgt.provision.rpc.relocate.Callback;

@AllArgsConstructor
@Slf4j
public class NodeScanner {

    private final CamelContext context;
    private final LocationAwareDetectorClient locationAwareDetectorClient;
    private final ForeignSourceRepository foreignSourceRepository;

    public void scanNode(RequisitionNodeDTO node) throws Exception {
            context.addRoutes(createScheduledRoute(node));
    }

    private RouteBuilder createScheduledRoute(RequisitionNodeDTO node) {
        ForeignSourceDTO foreignSourceDTO = foreignSourceRepository.getForeignSource(node.getForeignId());

        //TODO: do we need to be more sophisticated on building the cron string?
        // Below is a format for doing simple intervals without a CRON string
        // String routeOrigin = String.format("customQuartz://%s?trigger.repeatInterval=%d&triggerStartDelay=20",node.getNodeLabel(), foreignSourceDTO.getScanInterval().getMillis());

        String routeOrigin = String.format("customQuartz://%s?cron=0 0/%d * 1/1 * ? *&triggerStartDelay=20",node.getNodeLabel(), foreignSourceDTO.getScanInterval().getStandardMinutes());

        String routeId = String.format("SCHEDULED-SCANNER-%s", node.getNodeLabel());
        
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                if (context.getRoute(routeId) != null) {
                    log.info("NodeScanner :: Scheduled scan/route  for {} already exists, replacing it!", routeId);
                    context.createFluentProducerTemplate().withDefaultEndpoint(String.format("controlbus:route?routeId=%s&action=stop", routeId)).withBody(null).send();
                    context.removeRoute(routeId);
                }

                from(routeOrigin).routeId(routeId).
                    process(new SimpleScanner(node));

                log.info("NodeScanner :: Created scheduled scan/route ({}) for {}", routeOrigin, routeId);
            }
        };
    }

    @RequiredArgsConstructor
    private class SimpleScanner implements Processor {

        private final RequisitionNodeDTO node;

        @Override
        public void process(Exchange exchange) throws Exception {
            log.info("NodeScanner :: performing scheduled scan for {} on thread ({})", exchange.getFromRouteId(), Thread.currentThread().getName());

            PluginConfigDTO pluginConfigDTO = new PluginConfigDTO();
            pluginConfigDTO.setName("WebDetector");
            pluginConfigDTO.setPluginClass("org.opennms.netmgt.provision.detector.web.WebDetector");

            OnmsMonitoringLocation onmsMonitoringLocation = new OnmsMonitoringLocation();
            onmsMonitoringLocation.setLocationName(node.getLocation());

            //TODO: what to actually impl here? 
            Callback<Boolean> callback = new Callback<>() {
                @Override
                public void accept(Boolean t) {

                }

                @Override
                public Boolean apply(Throwable throwable) {
                    return null;
                }
            };
            node.getInterfaces().values().forEach(intrfc -> {
                log.info("Interface {}", intrfc);
                DetectorRunner detectorRunner = new DetectorRunner(locationAwareDetectorClient, pluginConfigDTO, node.getId(), intrfc.getIpAddress(), onmsMonitoringLocation, null);
                detectorRunner.supplyAsyncThenAccept(callback);
            });
        }
    }
}
