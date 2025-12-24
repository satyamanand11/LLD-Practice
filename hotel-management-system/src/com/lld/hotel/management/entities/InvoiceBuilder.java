package com.lld.hotel.management.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder Pattern for Invoice construction
 * Supports complex invoice creation with services, taxes, and discounts
 */
public class InvoiceBuilder {
    private int invoiceId;
    private int bookingId;
    private BigDecimal baseAmount = BigDecimal.ZERO;
    private final List<ServiceItem> services = new ArrayList<>();
    private BigDecimal taxRate = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public InvoiceBuilder setInvoiceId(int invoiceId) {
        if (invoiceId <= 0) {
            throw new IllegalArgumentException("invoiceId must be positive");
        }
        this.invoiceId = invoiceId;
        return this;
    }

    public InvoiceBuilder setBooking(int bookingId) {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("bookingId must be positive");
        }
        this.bookingId = bookingId;
        return this;
    }

    public InvoiceBuilder setBaseAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("baseAmount must be non-negative");
        }
        this.baseAmount = baseAmount;
        return this;
    }

    public InvoiceBuilder addService(String serviceName, BigDecimal price, int quantity) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must be non-negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        services.add(new ServiceItem(serviceName, price, quantity));
        return this;
    }

    public InvoiceBuilder applyTax(BigDecimal taxRate) {
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("taxRate must be non-negative");
        }
        this.taxRate = taxRate;
        return this;
    }

    public InvoiceBuilder applyDiscount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("discountAmount must be non-negative");
        }
        this.discountAmount = discountAmount;
        return this;
    }

    public Invoice build() {
        validate();
        
        BigDecimal servicesTotal = services.stream()
                .map(item -> item.price.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal subtotal = baseAmount.add(servicesTotal);
        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(new BigDecimal("100"));
        BigDecimal totalBeforeDiscount = subtotal.add(taxAmount);
        BigDecimal finalAmount = totalBeforeDiscount.subtract(discountAmount);
        
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        
        Invoice invoice = new Invoice(invoiceId, bookingId, finalAmount);
        return invoice;
    }

    private void validate() {
        if (invoiceId <= 0) {
            throw new IllegalStateException("invoiceId is required");
        }
        if (bookingId <= 0) {
            throw new IllegalStateException("bookingId is required");
        }
    }

    private static class ServiceItem {
        final String name;
        final BigDecimal price;
        final int quantity;

        ServiceItem(String name, BigDecimal price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }
}

