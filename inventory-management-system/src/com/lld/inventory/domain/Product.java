package com.lld.inventory.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Product entity representing a product in the inventory system
 */
public class Product {
    private final String productId;
    private final String name;
    private final String description;
    private final String sku;
    private final Instant createdAt;
    private int lowStockThreshold;

    public Product(String productId, String name, String description, String sku, int lowStockThreshold) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku cannot be null or empty");
        }
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException("lowStockThreshold cannot be negative");
        }

        this.productId = productId;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.lowStockThreshold = lowStockThreshold;
        this.createdAt = Instant.now();
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSku() {
        return sku;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", lowStockThreshold=" + lowStockThreshold +
                '}';
    }
}

