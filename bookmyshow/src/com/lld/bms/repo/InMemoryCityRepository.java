package com.lld.bms.repo;

import com.lld.bms.domain.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCityRepository implements CityRepository {
    private final ConcurrentHashMap<String, City> store = new ConcurrentHashMap<>();

    @Override
    public void save(City city) {
        store.put(city.getId(), city);
    }

    @Override
    public Optional<City> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<City> findAll() {
        return new ArrayList<>(store.values());
    }
}
