package com.lld.kvstore.interfaces;

import com.lld.kvstore.models.Result;
import com.lld.kvstore.observers.StoreObserver;

public interface KeyValueStore {
    Result<Boolean> put(String key, Object value);
    Result<Value> get(String key);
    Result<Boolean> delete(String key);
    Result<Boolean> exists(String key);
    Result<Integer> size();
    Result<Boolean> clear();
    Result<java.util.Set<String>> keys();
    <T> Result<T> executeCommand(Command<T> command);
    void addObserver(StoreObserver observer);
    void removeObserver(StoreObserver observer);
    Result<com.lld.kvstore.enums.ValueType> getValueType(String key);
}
