package com.lld.bms.service.pricing;

import com.lld.bms.domain.Show;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

public final class WeekendModifier implements PriceModifier {
    private static final Set<DayOfWeek> WEEKEND =
            EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final int percent;

    public WeekendModifier(int percent) {
        if (percent <= 0) {
            throw new IllegalArgumentException("percent must be > 0");
        }
        this.percent = percent;
    }

    @Override
    public int apply(int currentPrice, Show show) {
        DayOfWeek dow = show.getStartTime().getDayOfWeek();
        if (WEEKEND.contains(dow)) {
            return currentPrice * percent / 100;
        }
        return currentPrice;
    }
}
