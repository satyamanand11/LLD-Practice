package com.lld.hotel.management.pattern.decorator;

import java.math.BigDecimal;

/**
 * Decorator Pattern - Base Decorator
 * Abstract decorator that wraps a PriceCalculator
 */
public abstract class PriceDecorator implements PriceCalculator {
    protected final PriceCalculator wrapped;

    public PriceDecorator(PriceCalculator wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped calculator cannot be null");
        }
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice() {
        return wrapped.calculatePrice();
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription();
    }
}

