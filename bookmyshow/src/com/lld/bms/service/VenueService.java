package com.lld.bms.service;

import com.lld.bms.domain.Screen;
import com.lld.bms.domain.ScreenType;
import com.lld.bms.domain.Seat;
import com.lld.bms.domain.Venue;
import com.lld.bms.repo.CityRepository;
import com.lld.bms.repo.ScreenRepository;
import com.lld.bms.repo.VenueRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class VenueService {
    private final VenueRepository venueRepository;
    private final ScreenRepository screenRepository;
    private final CityRepository cityRepository;

    public VenueService(VenueRepository venueRepository,
                        ScreenRepository screenRepository,
                        CityRepository cityRepository) {
        this.venueRepository = Objects.requireNonNull(venueRepository, "venueRepository cannot be null");
        this.screenRepository = Objects.requireNonNull(screenRepository, "screenRepository cannot be null");
        this.cityRepository = Objects.requireNonNull(cityRepository, "cityRepository cannot be null");
    }

    public Venue addVenue(String cityId, String name, String address) {
        validateNonBlank(cityId, "cityId");
        validateNonBlank(name, "name");
        validateNonBlank(address, "address");
        cityRepository.findById(cityId)
                .orElseThrow(() -> new NoSuchElementException("City not found: " + cityId));
        Venue venue = new Venue(UUID.randomUUID().toString(), cityId, name, address);
        venueRepository.save(venue);
        return venue;
    }

    public Screen addScreen(String venueId, String name, ScreenType type, List<Seat> seats) {
        validateNonBlank(venueId, "venueId");
        validateNonBlank(name, "name");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(seats, "seats cannot be null");
        if (seats.isEmpty()) {
            throw new IllegalArgumentException("Screen must have at least one seat");
        }
        venueRepository.findById(venueId)
                .orElseThrow(() -> new NoSuchElementException("Venue not found: " + venueId));
        Screen screen = new Screen(UUID.randomUUID().toString(), venueId, name, type, seats);
        screenRepository.save(screen);
        return screen;
    }

    public Venue getVenue(String venueId) {
        validateNonBlank(venueId, "venueId");
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new NoSuchElementException("Venue not found: " + venueId));
    }

    public Screen getScreen(String screenId) {
        validateNonBlank(screenId, "screenId");
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new NoSuchElementException("Screen not found: " + screenId));
    }

    public List<Venue> listVenuesInCity(String cityId) {
        validateNonBlank(cityId, "cityId");
        return venueRepository.findByCityId(cityId);
    }

    public List<Screen> listScreens(String venueId) {
        validateNonBlank(venueId, "venueId");
        return screenRepository.findByVenueId(venueId);
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
    }
}
