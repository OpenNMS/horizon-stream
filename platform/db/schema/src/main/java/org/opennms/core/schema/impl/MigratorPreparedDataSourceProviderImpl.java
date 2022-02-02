package org.opennms.core.schema.impl;

import org.opennms.core.schema.OpenNMSDatabasePrehook;
import org.opennms.core.schema.PreparedDataSourceProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Takes the "early" datasource for accessing the opennms database, executes the DB Setup + Migration PreHook, and
 * returns the "prepared" datasource, ready for Application use.
 *
 * Note that this replaces the pre-hook logic from pax-jdbc-config; the pax bundle is not in-use here due to problems
 * sharing settings shared with the migrator.
 */
public class MigratorPreparedDataSourceProviderImpl implements PreparedDataSourceProvider {

    private OpenNMSDatabasePrehook prehook;
    private DataSource earlyDatasource;

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

//========================================
// Operations
//----------------------------------------

    @Override
    public DataSource retrieve() throws SQLException {
        prehook.prepare(earlyDatasource);

        // The early datasource is now prepared, so we can just return it, as the datasource itself hasn't changed,
        //  but the DB it accesses is now setup so the datasource will function.
        return earlyDatasource;
    }
}
