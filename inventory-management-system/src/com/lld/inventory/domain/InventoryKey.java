package com.lld.inventory.domain;

import java.util.Objects;

/**
 * Immutable value object representing the unique identity of inventory.
 * Used for:
 * - Map keys in repository
 * - Lock identity
 * - Deadlock-free ordering (implements Comparable)
 */
public class InventoryKey implements Comparable<InventoryKey> {
    private final String warehouseId;
    private final String productId;

    public InventoryKey(String warehouseId, String productId) {
        if (warehouseId == null || warehouseId.isBlank()) {
            throw new IllegalArgumentException("warehouseId cannot be null or empty");
        }
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or empty");
        }
        this.warehouseId = warehouseId;
        this.productId = productId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryKey that = (InventoryKey) o;
        return Objects.equals(warehouseId, that.warehouseId) &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouseId, productId);
    }

    /**
     * Implements Comparable for deterministic ordering.
     * Used for deadlock prevention when acquiring multiple locks.
     * Order: warehouseId first, then productId (both alphabetically).
     */
    @Override
    public int compareTo(InventoryKey other) {
        int warehouseCompare = this.warehouseId.compareTo(other.warehouseId);
        if (warehouseCompare != 0) {
            return warehouseCompare;
        }
        return this.productId.compareTo(other.productId);
    }

    @Override
    public String toString() {
        return "InventoryKey{" +
                "warehouseId='" + warehouseId + '\'' +
                ", productId='" + productId + '\'' +
                '}';
    }
}

