package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Hotel;

import java.util.Optional;

public interface HotelRepository {

    Optional<Hotel> findById(int hotelId);

    void save(Hotel hotel);
}
