package com.lld.kvstore.observers;

import com.lld.kvstore.interfaces.Value;

public interface StoreObserver {
    void onPut(String key, Value value);
    void onGet(String key, Value value);
    void onDelete(String key);
    void onError(String operation, String error);
    void onClear();
    void onSizeChange(int newSize);
}