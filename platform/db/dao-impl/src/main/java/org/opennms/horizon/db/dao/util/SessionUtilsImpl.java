package org.opennms.horizon.db.dao.util;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.opennms.horizon.db.dao.api.SessionUtils;

public class SessionUtilsImpl implements SessionUtils {
    private final JpaTemplate jpa;

    public SessionUtilsImpl(JpaTemplate jpa) {
        this.jpa = Objects.requireNonNull(jpa);
    }

    @Override
    public <V> V withTransaction(Supplier<V> supplier) {
        return jpa.txExpr(TransactionType.Required, em -> supplier.get());
    }

    @Override
    public <V> V withReadOnlyTransaction(Supplier<V> supplier) {
        return withTransaction(supplier);
    }

}
