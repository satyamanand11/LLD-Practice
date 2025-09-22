package com.lld.kvstore.infrastructure;

import java.util.Optional;

public interface Storage {
    Optional<StoredEntry> read(String key);
    void write(String key, StoredEntry entry);
    void delete(String key);
}
