package com.lld.inventory.strategy;

import com.lld.inventory.domain.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Default strategy that returns all eligible warehouses.
 * This is the simplest implementation - returns all warehouses that can fulfill the request.
 */
public class AllEligibleWarehousesStrategy implements WarehouseSelectionStrategy {
    @Override
    public List<String> selectWarehouses(List<InventoryItem> eligibleItems, int requiredQuantity) {
        if (eligibleItems == null) {
            return new ArrayList<>();
        }
        List<String> warehouseIds = new ArrayList<>();
        for (InventoryItem item : eligibleItems) {
            if (item.getQuantity() >= requiredQuantity) {
                warehouseIds.add(item.getKey().getWarehouseId());
            }
        }
        return warehouseIds;
    }
}

