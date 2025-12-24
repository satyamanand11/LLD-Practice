package com.lld.hotel.management.pattern.decorator;

import java.math.BigDecimal;

/**
 * Decorator Pattern - Concrete Decorator
 * Adds seasonal surge pricing
 */
public class SeasonalSurgeDecorator extends PriceDecorator {
    private final BigDecimal surgePercentage;
    private final String season;

    public SeasonalSurgeDecorator(PriceCalculator wrapped, BigDecimal surgePercentage, String season) {
        super(wrapped);
        if (surgePercentage == null || surgePercentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("surgePercentage must be non-negative");
        }
        if (season == null || season.isBlank()) {
            throw new IllegalArgumentException("season is required");
        }
        this.surgePercentage = surgePercentage;
        this.season = season;
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
               String.format(" + %s Seasonal Surge (%s%%)", season, surgePercentage);
    }
}

