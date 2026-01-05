package com.lld.inventory.repository;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.domain.InventoryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of InventoryRepository.
 * 
 * Implementation details:
 * - Backed by ConcurrentHashMap<InventoryKey, InventoryItem>
 * - upsert() behaves like DB insert/update
 * - Thread-safe at map level, not at business level
 * - Business-level thread-safety is handled by LockManager in service layer
 */
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<InventoryKey, InventoryItem> store = new ConcurrentHashMap<>();

    @Override
    public Optional<InventoryItem> findByKey(InventoryKey key) {
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void upsert(InventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        store.put(item.getKey(), item);
    }

    @Override
    public List<InventoryItem> findByProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or empty");
        }
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item : store.values()) {
            if (item.getKey().getProductId().equals(productId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<InventoryItem> findByWarehouseId(String warehouseId) {
        if (warehouseId == null || warehouseId.isBlank()) {
            throw new IllegalArgumentException("warehouseId cannot be null or empty");
        }
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item : store.values()) {
            if (item.getKey().getWarehouseId().equals(warehouseId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public List<InventoryItem> findAll() {
        return new ArrayList<>(store.values());
    }
}

