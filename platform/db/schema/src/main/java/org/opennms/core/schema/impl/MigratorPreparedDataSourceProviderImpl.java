package org.opennms.core.schema.impl;

import org.opennms.core.schema.OpenNMSDatabasePrehook;
import org.opennms.core.schema.PreparedDataSourceProvider;

import javax.sql.DataSource;
import java.sql.SQLException;

public class MigratorPreparedDataSourceProviderImpl implements PreparedDataSourceProvider {

    private OpenNMSDatabasePrehook prehook;
    private DataSource rawDatasource;

    public OpenNMSDatabasePrehook getPrehook() {
        return prehook;
    }

    public void setPrehook(OpenNMSDatabasePrehook prehook) {
        this.prehook = prehook;
    }

    public DataSource getRawDatasource() {
        return rawDatasource;
    }

    public void setRawDatasource(DataSource rawDatasource) {
        this.rawDatasource = rawDatasource;
    }

//========================================
//
//----------------------------------------

    @Override
    public DataSource retrieve() throws SQLException {
        prehook.prepare(rawDatasource);

        // The raw datasource is now cooked, so we can just return it
        return rawDatasource;
    }
}
