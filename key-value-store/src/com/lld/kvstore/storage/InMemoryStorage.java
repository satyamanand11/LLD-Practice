package com.lld.kvstore.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStorage implements Storage {
    private final Map<String, StorageEntry> storage;
    
    public InMemoryStorage() {
        this.storage = new ConcurrentHashMap<>();
    }
    
    @Override
    public Optional<StorageEntry> read(String key) {
        return Optional.ofNullable(storage.get(key));
    }
    
    @Override
    public void write(String key, StorageEntry entry) {
        storage.put(key, entry);
    }
    
    @Override
    public void delete(String key) {
        storage.remove(key);
    }
}
