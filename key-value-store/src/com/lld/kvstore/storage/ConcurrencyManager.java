package com.lld.kvstore.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrencyManager {
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> locks;
    
    public ConcurrencyManager() {
        this.locks = new ConcurrentHashMap<>();
    }
    
    public ReentrantReadWriteLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
    }
}
