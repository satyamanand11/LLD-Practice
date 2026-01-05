package com.lld.inventory.observer.impl;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.observer.LowStockListener;

import java.time.Instant;

/**
 * Console logger implementation of LowStockListener.
 * Logs low stock alerts to the console.
 */
public class ConsoleLogListener implements LowStockListener {
    @Override
    public void onLowStock(InventoryItem item) {
        System.out.println(String.format(
                "[LOW STOCK ALERT] %s | Warehouse: %s, Product: %s | " +
                        "Current: %d, Threshold: %d",
                Instant.now(),
                item.getKey().getWarehouseId(),
                item.getKey().getProductId(),
                item.getQuantity(),
                item.getLowStockThreshold()
        ));
    }

    @Override
    public String getListenerType() {
        return "ConsoleLogListener";
    }
}

