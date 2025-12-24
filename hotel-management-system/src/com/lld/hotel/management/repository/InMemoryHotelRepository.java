package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Hotel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryHotelRepository implements HotelRepository {

    private final Map<Integer, Hotel> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Hotel> findById(int hotelId) {
        return Optional.ofNullable(store.get(hotelId));
    }

    @Override
    public void save(Hotel hotel) {
        store.put(hotel.getHotelId(), hotel);
    }
}
