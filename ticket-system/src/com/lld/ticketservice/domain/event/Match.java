package com.lld.ticketservice.domain.event;

public class Match extends Event {
    private final String sport;
    private final String teams;

    public Match(int id, String title, String sport, String teams) {
        super(id, title);
        this.sport = sport;
        this.teams = teams;
    }

    public String getSport() {
        return sport;
    }

    public String getTeams() {
        return teams;
    }
}
