package com.lld.cricbuzz.events;

import com.lld.cricbuzz.domain.match.Wicket;

/**
 * Event published when a wicket falls
 */
public class WicketEvent extends MatchEvent {
    private final Wicket wicket;
    private final String batterId;

    public WicketEvent(String eventId, String matchId, Wicket wicket) {
        super(eventId, matchId);
        this.wicket = wicket;
        this.batterId = wicket.getBatterId();
    }

    public Wicket getWicket() {
        return wicket;
    }

    public String getBatterId() {
        return batterId;
    }
}

