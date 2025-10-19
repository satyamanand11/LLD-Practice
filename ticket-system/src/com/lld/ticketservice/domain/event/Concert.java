package com.lld.ticketservice.domain.event;

public class Concert extends Event {
    private final String artist;
    private final String genre;

    public Concert(int id, String title, String artist, String genre) {
        super(id, title);
        this.artist = artist;
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }
}
