package com.lld.bms.service;

import com.lld.bms.domain.City;
import com.lld.bms.repo.CityRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = Objects.requireNonNull(cityRepository, "cityRepository cannot be null");
    }

    public City addCity(String name, String state) {
        validateNonBlank(name, "name");
        validateNonBlank(state, "state");
        City city = new City(UUID.randomUUID().toString(), name, state);
        cityRepository.save(city);
        return city;
    }

    public City getCity(String id) {
        validateNonBlank(id, "id");
        return cityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("City not found: " + id));
    }

    public List<City> listCities() {
        return cityRepository.findAll();
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
    }
}
