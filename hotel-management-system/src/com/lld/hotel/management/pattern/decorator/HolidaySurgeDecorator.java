package com.lld.hotel.management.pattern.decorator;

import java.math.BigDecimal;

/**
 * Decorator Pattern - Concrete Decorator
 * Adds holiday surge pricing
 */
public class HolidaySurgeDecorator extends PriceDecorator {
    private final BigDecimal surgePercentage;

    public HolidaySurgeDecorator(PriceCalculator wrapped, BigDecimal surgePercentage) {
        super(wrapped);
        if (surgePercentage == null || surgePercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("surgePercentage must be non-negative");
        }
        this.surgePercentage = surgePercentage;
    }

    @Override
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = wrapped.calculatePrice();
        BigDecimal surgeAmount = basePrice.multiply(surgePercentage)
                .divide(new BigDecimal("100"));
        return basePrice.add(surgeAmount);
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + 
               String.format(" + Holiday Surge (%s%%)", surgePercentage);
    }
}

