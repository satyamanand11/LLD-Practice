package com.lld.inventory.factory;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.domain.InventoryKey;

/**
 * Factory for creating new inventory items.
 * Centralizes creation logic and applies default values.
 * 
 * Used by InventoryServiceImpl when creating inventory rows if missing.
 */
public class InventoryItemFactory {
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /**
     * Creates a new InventoryItem with default values.
     * Default low stock threshold is 10.
     */
    public InventoryItem create(InventoryKey key) {
        return create(key, 0, DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Creates a new InventoryItem with specified initial quantity.
     * Uses default low stock threshold.
     */
    public InventoryItem create(InventoryKey key, int initialQuantity) {
        return create(key, initialQuantity, DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Creates a new InventoryItem with specified initial quantity and threshold.
     */
    public InventoryItem create(InventoryKey key, int initialQuantity, int lowStockThreshold) {
        return new InventoryItem(key, initialQuantity, lowStockThreshold);
    }
}

