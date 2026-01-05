package com.lld.inventory.service;

import com.lld.inventory.domain.InventoryItem;

import java.util.List;

/**
 * Service interface for inventory operations.
 * 
 * Responsibilities:
 * - Enforce business invariants
 * - Coordinate repository + locks
 */
public interface InventoryService {
    /**
     * Adds stock to a specific warehouse.
     * Creates inventory row if missing.
     * 
     * @param warehouseId The warehouse ID
     * @param productId The product ID
     * @param quantity The quantity to add (must be positive)
     */
    void addStock(String warehouseId, String productId, int quantity);

    /**
     * Removes stock from a specific warehouse.
     * Rejects if insufficient stock.
     * 
     * @param warehouseId The warehouse ID
     * @param productId The product ID
     * @param quantity The quantity to remove (must be positive)
     * @throws IllegalStateException if insufficient stock
     */
    void removeStock(String warehouseId, String productId, int quantity);

    /**
     * Transfers stock between warehouses.
     * Atomic operation: removes from source, adds to destination.
     * 
     * @param fromWarehouseId Source warehouse ID
     * @param toWarehouseId Destination warehouse ID
     * @param productId The product ID
     * @param quantity The quantity to transfer (must be positive)
     * @throws IllegalStateException if source has insufficient stock
     */
    void transferStock(String fromWarehouseId, String toWarehouseId, String productId, int quantity);

    /**
     * Checks availability across all warehouses for a given product and quantity.
     * Returns list of warehouses that can fulfill the request.
     * 
     * @param productId The product ID
     * @param requiredQuantity The required quantity
     * @return List of warehouses with availability information
     */
    List<WarehouseAvailability> checkAvailability(String productId, int requiredQuantity);

    /**
     * Gets inventory item for a specific warehouse and product.
     * 
     * @param warehouseId The warehouse ID
     * @param productId The product ID
     * @return The inventory item, or null if not found
     */
    InventoryItem getInventory(String warehouseId, String productId);
}

