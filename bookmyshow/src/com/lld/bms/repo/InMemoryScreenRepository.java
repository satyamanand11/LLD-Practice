package com.lld.bms.repo;

import com.lld.bms.domain.Screen;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryScreenRepository implements ScreenRepository {
    private final ConcurrentHashMap<String, Screen> store = new ConcurrentHashMap<>();

    @Override
    public void save(Screen screen) {
        store.put(screen.getId(), screen);
    }

    @Override
    public Optional<Screen> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Screen> findByVenueId(String venueId) {
        return store.values().stream()
                .filter(s -> s.getVenueId().equals(venueId))
                .collect(Collectors.toList());
    }
}
