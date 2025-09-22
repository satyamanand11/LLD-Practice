package com.lld.kvstore.storage;

import java.util.Optional;

public interface Storage {
    Optional<StorageEntry> read(String key);
    void write(String key, StorageEntry entry);
    void delete(String key);
}
