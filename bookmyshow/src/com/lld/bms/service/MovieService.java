package com.lld.bms.service;

import com.lld.bms.domain.Movie;
import com.lld.bms.repo.MovieRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = Objects.requireNonNull(movieRepository, "movieRepository cannot be null");
    }

    public Movie addMovie(String title, int durationMinutes, String genre, String language) {
        validateNonBlank(title, "title");
        validateNonBlank(genre, "genre");
        validateNonBlank(language, "language");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be > 0");
        }
        Movie movie = new Movie(UUID.randomUUID().toString(), title, durationMinutes, genre, language);
        movieRepository.save(movie);
        return movie;
    }

    public Movie getMovie(String id) {
        validateNonBlank(id, "id");
        return movieRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Movie not found: " + id));
    }

    public List<Movie> searchByTitle(String fragment) {
        if (fragment == null || fragment.isBlank()) {
            throw new IllegalArgumentException("fragment cannot be null or blank");
        }
        return movieRepository.findByTitleContaining(fragment);
    }

    public List<Movie> listAll() {
        return movieRepository.findAll();
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
    }
}
