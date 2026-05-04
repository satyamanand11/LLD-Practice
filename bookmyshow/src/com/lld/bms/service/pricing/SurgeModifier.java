package com.lld.bms.service.pricing;

import com.lld.bms.domain.Show;

import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Applies an int-percent uplift once the show's occupancy reaches a threshold.
 * Percent is expressed as 100 = no change, 125 = +25%.
 *
 * Occupancy lookup is supplied as a function (typically a method reference like
 * `showService::occupancyPercent`) so this class doesn't depend on the service layer.
 */
public final class SurgeModifier implements PriceModifier {
    private final ToIntFunction<String> occupancyByShowId;
    private final int thresholdPercent;
    private final int percent;

    public SurgeModifier(ToIntFunction<String> occupancyByShowId, int thresholdPercent, int percent) {
        this.occupancyByShowId = Objects.requireNonNull(occupancyByShowId, "occupancyByShowId cannot be null");
        if (thresholdPercent < 0 || thresholdPercent > 100) {
            throw new IllegalArgumentException("thresholdPercent must be in [0, 100]");
        }
        if (percent <= 0) {
            throw new IllegalArgumentException("percent must be > 0");
        }
        this.thresholdPercent = thresholdPercent;
        this.percent = percent;
    }

    @Override
    public int apply(int currentPrice, Show show) {
        int occ = occupancyByShowId.applyAsInt(show.getId());
        if (occ >= thresholdPercent) {
            return currentPrice * percent / 100;
        }
        return currentPrice;
    }
}
