package com.lld.ticketservice.services;

import com.lld.ticketservice.domain.show.Show;
import com.lld.ticketservice.managers.seat.SeatManager;
import com.lld.ticketservice.managers.show.ShowManager;
import com.lld.ticketservice.pricing.PricingStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PricingService {
    private final PricingStrategy strategy;
    private final SeatManager seatMgr;
    private final ShowManager showMgr;

    public PricingService(PricingStrategy strategy, SeatManager seatMgr, ShowManager showMgr) {
        this.strategy = strategy;
        this.seatMgr = seatMgr;
        this.showMgr = showMgr;
    }

    public int quoteSeat(int showId, int seatNumber) {
        Show show = showMgr.getShow(showId);
        int base = seatMgr.getSeatBasePrice(show.getVenueId(), show.getScreenNumber(), seatNumber);
        return strategy.apply(base, show);
    }

    public Map<Integer, Integer> quoteSeats(int showId, List<Integer> seats) {
        Map<Integer, Integer> out = new LinkedHashMap<>();
        for (int s : seats) out.put(s, quoteSeat(showId, s));
        return out;
    }

    public int totalForSeats(int showId, List<Integer> seats) {
        return quoteSeats(showId, seats).values().stream().mapToInt(Integer::intValue).sum();
    }
}
