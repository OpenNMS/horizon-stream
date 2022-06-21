package org.opennms.netmgt.provision.persistence.model;

import com.google.gson.Gson;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.opennms.netmgt.provision.persistence.dto.RequisitionDTO;

public class RequsitionJsonType implements UserType {
    Gson gson = new Gson();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class<RequisitionDTO> returnedClass() {
        return RequisitionDTO.class;
    }

    @Override
    public boolean equals(Object o, Object o1) throws HibernateException {
        return false;
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        return ((RequisitionDTO)o).hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        final String requisitionStr = resultSet.getString(strings[0]);
        if (requisitionStr == null) {
            return null;
        }
        try {
            return gson.fromJson(requisitionStr, returnedClass());
        } catch (final Exception ex) {
            throw new RuntimeException("Persistence Read: Failed to convert String to Requisition: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int idx, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value == null) {
            preparedStatement.setNull(idx, Types.OTHER);
            return;
        }
        try {
            String requistionStr = gson.toJson(value);
            preparedStatement.setObject(idx, requistionStr, Types.OTHER);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to convert Invoice to String: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        if (o instanceof RequisitionDTO) {
            RequisitionDTO dto = (RequisitionDTO) o;
            String dtoStr = gson.toJson(dto);
            RequisitionDTO copyDto = gson.fromJson(dtoStr, RequisitionDTO.class);
            return copyDto;
        }
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        return null;
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        return null;
    }

    @Override
    public Object replace(Object o, Object o1, Object o2) throws HibernateException {
        return null;
    }
}
