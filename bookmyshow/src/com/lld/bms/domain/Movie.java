package com.lld.bms.domain;

public class Movie {
    private final String id;
    private final String title;
    private final int durationMinutes;
    private final String genre;
    private final String language;

    public Movie(String id, String title, int durationMinutes, String genre, String language) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.language = language;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getGenre() { return genre; }
    public String getLanguage() { return language; }
}
