package com.lld.kvstore.core;

import com.lld.kvstore.types.Value;
import com.lld.kvstore.types.Result;
import java.util.Collection;

public interface KeyValueStore {
    Result<Void> setPrimitive(String key, Object value);
    Result<Void> setList(String key, Collection<?> values);
    Result<Void> setSet(String key, Collection<?> values);
    Result<Value> get(String key);
    Result<Void> deleteKey(String key);
    Result<Void> addToCollection(String key, Collection<?> values);
    Result<Collection<?>> fetchFromCollection(String key, int limit);
    Result<Void> removeFromCollection(String key, Collection<?> values);
}
