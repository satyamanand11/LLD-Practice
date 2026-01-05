package com.lld.inventory.domain;

import java.util.Objects;

/**
 * Represents a single inventory row.
 * Entity is mutable and guards only its own state.
 * No orchestration logic - that belongs in the service layer.
 */
public class InventoryItem {
    private final InventoryKey key;
    private int quantity;
    private int lowStockThreshold;

    public InventoryItem(InventoryKey key, int quantity, int lowStockThreshold) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException("lowStockThreshold cannot be negative");
        }
        this.key = key;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public InventoryKey getKey() {
        return key;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException("lowStockThreshold cannot be negative");
        }
        this.lowStockThreshold = lowStockThreshold;
    }

    /**
     * Increases the quantity by the specified amount.
     * Does not validate - service layer should validate business rules.
     */
    public void increase(int qty) {
        if (qty < 0) {
            throw new IllegalArgumentException("qty cannot be negative");
        }
        this.quantity += qty;
    }

    /**
     * Decreases the quantity by the specified amount.
     * Rejects operations that would result in negative inventory.
     * This is a defensive guard - service layer should also validate.
     */
    public void decrease(int qty) {
        if (qty < 0) {
            throw new IllegalArgumentException("qty cannot be negative");
        }
        if (this.quantity < qty) {
            throw new IllegalStateException(
                    String.format("Cannot decrease quantity by %d. Current quantity: %d. Key: %s",
                            qty, this.quantity, key));
        }
        this.quantity -= qty;
    }

    /**
     * Checks if the current quantity is at or below the low stock threshold.
     */
    public boolean isLow() {
        return quantity <= lowStockThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "key=" + key +
                ", quantity=" + quantity +
                ", lowStockThreshold=" + lowStockThreshold +
                ", isLow=" + isLow() +
                '}';
    }
}

