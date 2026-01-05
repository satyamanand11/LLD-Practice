package com.lld.inventory.strategy;

import com.lld.inventory.domain.InventoryItem;

import java.util.List;

/**
 * Strategy interface for selecting warehouses that can fulfill a request.
 * 
 * Input: List of eligible InventoryItem (items with sufficient stock)
 * Output: List of warehouseIds
 * 
 * Allows easy extension:
 * - Nearest warehouse
 * - Minimum stock depletion
 * - Cost-based selection
 */
public interface WarehouseSelectionStrategy {
    /**
     * Selects warehouses from eligible inventory items.
     * 
     * @param eligibleItems List of inventory items that have sufficient stock
     * @param requiredQuantity The quantity required
     * @return List of warehouse IDs that can fulfill the request
     */
    List<String> selectWarehouses(List<InventoryItem> eligibleItems, int requiredQuantity);
}

