package org.opennms.horizon.repository.api;

public interface BasicCRUD<T, K> {
     //TODO: figure out keys (stirng or integer)
     T read(String id);
     void delete(String id);
     K save(T entity);
     void update(T entity);
}
