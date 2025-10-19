package com.lld.ticketservice.domain.event;

public class Movie extends Event {
    private final String genre;
    private final String language;

    public Movie(int id, String title, String genre, String language) {
        super(id, title);
        this.genre = genre;
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }
}
