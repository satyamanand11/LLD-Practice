package com.lld.ticketservice.pricing;

import com.lld.ticketservice.domain.show.Show;

public class DemandTierPricingStrategy implements PricingStrategy {
    public int apply(int baseAmount, Show show) {
        int occ = show.occupancyPercent();
        if (occ > 80) return (int) (baseAmount * 1.5);
        if (occ > 50) return (int) (baseAmount * 1.2);
        return baseAmount;
    }
}
