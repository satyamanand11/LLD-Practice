package com.lld.hotel.management.pattern.decorator;

import java.math.BigDecimal;

/**
 * Decorator Pattern - Component Interface
 * Base interface for price calculation
 */
public interface PriceCalculator {
    BigDecimal calculatePrice();
    String getDescription();
}

