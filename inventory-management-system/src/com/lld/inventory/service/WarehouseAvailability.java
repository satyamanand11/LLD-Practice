package com.lld.inventory.service;

/**
 * DTO representing warehouse availability for a product.
 * Returned by checkAvailability() method.
 */
public class WarehouseAvailability {
    private final String warehouseId;
    private final int availableQuantity;

    public WarehouseAvailability(String warehouseId, int availableQuantity) {
        if (warehouseId == null || warehouseId.isBlank()) {
            throw new IllegalArgumentException("warehouseId cannot be null or empty");
        }
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("availableQuantity cannot be negative");
        }
        this.warehouseId = warehouseId;
        this.availableQuantity = availableQuantity;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    @Override
    public String toString() {
        return "WarehouseAvailability{" +
                "warehouseId='" + warehouseId + '\'' +
                ", availableQuantity=" + availableQuantity +
                '}';
    }
}

