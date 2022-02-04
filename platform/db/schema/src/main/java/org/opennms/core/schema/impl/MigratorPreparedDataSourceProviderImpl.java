package org.opennms.core.schema.impl;

import org.opennms.core.schema.OpenNMSDatabasePrehook;
import org.opennms.core.schema.PreparedDataSourceProvider;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Takes the "early" datasource for accessing the opennms database, executes the DB Setup + Migration PreHook, and
 * returns the "prepared" datasource, ready for Application use.
 *
 * Note that this replaces the pre-hook logic from pax-jdbc-config; the pax bundle is not in-use here due to problems
 * sharing settings shared with the migrator.
 */
public class MigratorPreparedDataSourceProviderImpl implements PreparedDataSourceProvider {

    private Logger log = LoggerFactory.getLogger(MigratorPreparedDataSourceProviderImpl.class);

    private OpenNMSDatabasePrehook prehook;
    private DataSource earlyDatasource;
    private PooledDataSourceFactory pooledDataSourceFactory;
    private DataSourceFactory dataSourceFactory;
    private Properties pooledDataSourceProperties;

//========================================
// Getters and Setters
//----------------------------------------

    public OpenNMSDatabasePrehook getPrehook() {
        return prehook;
    }

    public void setPrehook(OpenNMSDatabasePrehook prehook) {
        this.prehook = prehook;
    }

    public DataSource getEarlyDatasource() {
        return earlyDatasource;
    }

    public void setEarlyDatasource(DataSource earlyDatasource) {
        this.earlyDatasource = earlyDatasource;
    }

    public PooledDataSourceFactory getPooledDataSourceFactory() {
        return pooledDataSourceFactory;
    }

    public void setPooledDataSourceFactory(PooledDataSourceFactory pooledDataSourceFactory) {
        this.pooledDataSourceFactory = pooledDataSourceFactory;
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    public Properties getPooledDataSourceProperties() {
        return pooledDataSourceProperties;
    }

    public void setPooledDataSourceProperties(Properties pooledDataSourceProperties) {
        this.pooledDataSourceProperties = pooledDataSourceProperties;
    }

//========================================
// Operations
//----------------------------------------

    @Override
    public DataSource retrieve() throws SQLException {
        prehook.prepare(earlyDatasource);

        // Now prepare the pooled data source.
        DataSource pooledResult = pooledDataSourceFactory.create(dataSourceFactory, pooledDataSourceProperties);

        log.info("DATABASE HAS BEEN PREPARED");

        // Return the pooled datasource.
        return pooledResult;
    }
}
