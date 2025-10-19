package com.lld.ticketservice.pricing;

import com.lld.ticketservice.domain.show.Show;

public class SurgePricingStrategy implements PricingStrategy {
    private final double factor;

    public SurgePricingStrategy(double factor) {
        this.factor = factor;
    }

    public int apply(int baseAmount, Show show) {
        return (int) (baseAmount * factor);
    }
}
