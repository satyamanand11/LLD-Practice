package com.lld.hotel.management.pattern.decorator;

import java.math.BigDecimal;

/**
 * Decorator Pattern - Concrete Component
 * Base price without any modifiers
 */
public class BasePrice implements PriceCalculator {
    private final BigDecimal amount;
    private final String description;

    public BasePrice(BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        this.amount = amount;
        this.description = description;
    }

    @Override
    public BigDecimal calculatePrice() {
        return amount;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

