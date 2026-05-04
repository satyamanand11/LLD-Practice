package com.lld.bms.service.pricing;

import com.lld.bms.domain.Show;
import com.lld.bms.domain.ShowSeat;
import com.lld.bms.service.selection.AddOn;

import java.util.List;
import java.util.Objects;

/**
 * Computes the final price for a seat at booking time.
 *
 * Tier price is read from ShowSeat.basePrice (snapshot frozen at show creation).
 * Demand-sensitive adjustments are applied as an ordered list of PriceModifier
 * decorators. Add-on prices are summed at the end.
 */
public class PricingService {
    private final List<PriceModifier> modifiers;

    public PricingService(List<PriceModifier> modifiers) {
        Objects.requireNonNull(modifiers, "modifiers cannot be null");
        this.modifiers = List.copyOf(modifiers);
    }

    public int price(ShowSeat seat, Show show, List<AddOn> addOns) {
        int p = seat.getBasePrice();
        for (PriceModifier m : modifiers) {
            p = m.apply(p, show);
        }
        for (AddOn a : addOns) {
            p += a.price();
        }
        return p;
    }
}
