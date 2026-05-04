package com.lld.bms.repo;

import java.util.Optional;

public interface Repository<T, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
}
