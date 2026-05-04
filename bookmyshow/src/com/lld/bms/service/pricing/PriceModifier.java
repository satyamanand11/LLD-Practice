package com.lld.bms.service.pricing;

import com.lld.bms.domain.Show;

public interface PriceModifier {
    int apply(int currentPrice, Show show);
}
