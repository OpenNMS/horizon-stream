package org.opennms.netmgt.provision.persistence.model;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQL95Dialect;

public class RequisitionPostgreSQL95Dialect extends PostgreSQL95Dialect {
    public RequisitionPostgreSQL95Dialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
