package com.lld.inventory.observer.impl;

import com.lld.inventory.domain.InventoryItem;
import com.lld.inventory.observer.LowStockListener;

/**
 * Email alert implementation of LowStockListener.
 * In a real system, this would send actual emails.
 * For this LLD, it simulates email sending.
 */
public class EmailAlertListener implements LowStockListener {
    @Override
    public void onLowStock(InventoryItem item) {
        // In real system, this would send an email
        System.out.println(String.format(
                "[EMAIL ALERT] Low stock alert sent via email for " +
                        "Warehouse: %s, Product: %s, Quantity: %d",
                item.getKey().getWarehouseId(),
                item.getKey().getProductId(),
                item.getQuantity()
        ));
    }

    @Override
    public String getListenerType() {
        return "EmailAlertListener";
    }
}

