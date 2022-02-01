package org.opennms.core.schema;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

// TBD999: remove
public class DBManagedServiceFactory implements ManagedServiceFactory {

    private Logger log = LoggerFactory.getLogger(DBManagedServiceFactory.class);

    @Override
    public String getName() {
        return "datasourceProperties";
    }

    @Override
    public void updated(String s, Dictionary dictionary) throws ConfigurationException {
        log.info("TBD AAA-000: updated {}", s);
    }

    @Override
    public void deleted(String s) {
        log.info("TBD AAA-000: deleted {}", s);
    }
}
