package org.opennms.core.schema;

import java.sql.SQLException;

public interface PreparedDataSourceProvider {
    javax.sql.DataSource retrieve() throws SQLException;
}
