package com.lld.kvstore.infrastructure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockManager {
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> locks;
    
    public LockManager() {
        this.locks = new ConcurrentHashMap<>();
    }
    
    public ReentrantReadWriteLock forKey(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
    }
}
