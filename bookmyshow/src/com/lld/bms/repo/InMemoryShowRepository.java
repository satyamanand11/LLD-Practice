package com.lld.bms.repo;

import com.lld.bms.domain.Show;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryShowRepository implements ShowRepository {
    private final ConcurrentHashMap<String, Show> store = new ConcurrentHashMap<>();

    @Override
    public void save(Show show) {
        store.put(show.getId(), show);
    }

    @Override
    public Optional<Show> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Show> findByScreenId(String screenId) {
        return store.values().stream()
                .filter(s -> s.getScreenId().equals(screenId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Show> findByMovieId(String movieId) {
        return store.values().stream()
                .filter(s -> s.getMovieId().equals(movieId))
                .collect(Collectors.toList());
    }
}
