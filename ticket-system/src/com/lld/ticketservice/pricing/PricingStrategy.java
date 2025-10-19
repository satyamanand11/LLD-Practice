package com.lld.ticketservice.pricing;


import com.lld.ticketservice.domain.show.Show;
public interface PricingStrategy {
    int apply(int baseAmount, Show show);
}
