package com.lld.inventory.repository;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.domain.InventoryKey;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for inventory data access.
 * 
 * Important:
 * - Repository does NOT enforce business rules
 * - Repository does NOT handle locking
 * - Repository does NOT reject negative values
 * - Repository is just data access
 */
public interface InventoryRepository {
    /**
     * Fetches inventory by key.
     * Returns Optional.empty() if not found.
     */
    Optional<InventoryItem> findByKey(InventoryKey key);

    /**
     * Inserts or updates inventory (upsert behavior).
     * Behaves like DB insert/update - creates if absent, updates if present.
     */
    void upsert(InventoryItem item);

    /**
     * Queries all inventory items for a given product across all warehouses.
     * Returns empty list if product not found in any warehouse.
     */
    List<InventoryItem> findByProductId(String productId);

    /**
     * Queries all inventory items for a given warehouse.
     * Returns empty list if warehouse has no inventory.
     */
    List<InventoryItem> findByWarehouseId(String warehouseId);

    /**
     * Returns all inventory items in the system.
     */
    List<InventoryItem> findAll();
}

