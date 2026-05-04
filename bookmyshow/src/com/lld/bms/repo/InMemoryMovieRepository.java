package com.lld.bms.repo;

import com.lld.bms.domain.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryMovieRepository implements MovieRepository {
    private final ConcurrentHashMap<String, Movie> store = new ConcurrentHashMap<>();

    @Override
    public void save(Movie movie) {
        store.put(movie.getId(), movie);
    }

    @Override
    public Optional<Movie> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Movie> findByTitleContaining(String titleFragment) {
        String needle = titleFragment.toLowerCase();
        return store.values().stream()
                .filter(m -> m.getTitle().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> findAll() {
        return new ArrayList<>(store.values());
    }
}
