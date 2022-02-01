package org.opennms.core.schema;

import org.apache.aries.component.dsl.CachingServiceReference;
import org.apache.aries.component.dsl.OSGi;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class SchemaBundleActivator implements BundleActivator {

    private Logger log = LoggerFactory.getLogger(SchemaBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        OSGi<Void> program =
            OSGi.
                    serviceReferences(DataSource.class)
                        .foreach(this::processDataSourceService)
            ;

        program.run(bundleContext);
        // OSGi.
        //         serviceReferences(DataSource.class, "(|(dataSourceName=opennms)(dataSourceName=opennmsAdmin))")
        //             .foreach(this::processDataSourceService)
        // ;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
    }

//========================================
//
//----------------------------------------

    private void processDataSourceService(CachingServiceReference<DataSource> serviceReference) {
        log.info("TBD BBB-000: have service reference: property-keys={}, bundle-id={}",
                serviceReference.getPropertyKeys(),
                serviceReference.getServiceReference().getBundle().getBundleId()
        );
    }
}
