package com.lld.inventory.observer;

import com.lld.inventory.domain.InventoryItem;

/**
 * Interface for alert mechanisms that listen to low stock events.
 * 
 * Example implementations:
 * - Console logger
 * - Email notification
 * - SMS notification
 * - Slack notification
 * - Metrics collector
 */
public interface LowStockListener {
    /**
     * Called when inventory falls to or below the low stock threshold.
     * 
     * @param item The inventory item that triggered the low stock alert
     */
    void onLowStock(InventoryItem item);

    /**
     * Returns the type/name of this listener (for logging/debugging).
     */
    String getListenerType();
}

