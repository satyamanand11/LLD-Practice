package com.lld.kvstore.core;

import com.lld.kvstore.types.Value;
import com.lld.kvstore.types.Result;
import java.util.Collection;

public interface KeyValueStore {
    Result<Void> setPrimitive(String key, Object value);
    <T> Result<Void> setList(String key, Collection<T> values);
    <T> Result<Void> setSet(String key, Collection<T> values);

    Result<Value> get(String key);
    Result<Void> deleteKey(String key);

    <T> Result<Void> addToCollection(String key, Collection<T> values);
    <T> Result<Collection<T>> fetchFromCollection(String key, int limit);
    <T> Result<Void> removeFromCollection(String key, Collection<T> values);
}
