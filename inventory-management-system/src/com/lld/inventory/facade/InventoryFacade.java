package com.lld.inventory.facade;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.service.WarehouseAvailability;

import java.util.List;

/**
 * Public API of the inventory management system.
 * Entry point for controllers / clients.
 * 
 * Characteristics:
 * - Thin delegation layer
 * - No business logic
 * - Hides internal services and repositories
 */
public interface InventoryFacade {
    /**
     * Adds stock to a specific warehouse (receiving shipments).
     */
    void addStock(String warehouseId, String productId, int quantity);

    /**
     * Removes stock from a specific warehouse (fulfilling orders).
     * Throws exception if insufficient stock.
     */
    void removeStock(String warehouseId, String productId, int quantity);

    /**
     * Checks availability: given a product and quantity, returns which warehouses can fulfill it.
     */
    List<WarehouseAvailability> checkAvailability(String productId, int requiredQuantity);

    /**
     * Transfers stock between warehouses.
     * Atomic operation: removes from source, adds to destination.
     */
    void transferStock(String fromWarehouseId, String toWarehouseId, String productId, int quantity);

    /**
     * Gets inventory item for a specific warehouse and product.
     * Returns null if not found.
     */
    InventoryItem getInventory(String warehouseId, String productId);
}

