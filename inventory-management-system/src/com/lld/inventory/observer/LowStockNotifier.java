package com.lld.inventory.observer;

import com.lld.inventory.domain.InventoryItem;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Maintains a list of LowStockListener and notifies them when inventory crosses threshold.
 * 
 * Thread-safe: Uses CopyOnWriteArrayList for concurrent access.
 */
public class LowStockNotifier {
    private final List<LowStockListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers a listener to receive low stock notifications.
     */
    public void registerListener(LowStockListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.add(listener);
    }

    /**
     * Unregisters a listener.
     */
    public void unregisterListener(LowStockListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners about a low stock event.
     * Called by InventoryService when inventory quantity changes and is low.
     */
    public void notifyLowStock(InventoryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        for (LowStockListener listener : listeners) {
            try {
                listener.onLowStock(item);
            } catch (Exception e) {
                // Log error but don't fail the operation if one listener fails
                System.err.println("Error notifying listener " + listener.getListenerType() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Returns the number of registered listeners.
     */
    public int getListenerCount() {
        return listeners.size();
    }
}

