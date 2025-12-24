package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.BookingServiceEntity;

import java.util.List;
import java.util.Optional;

public interface BookingServiceRepository {
    Optional<BookingServiceEntity> findById(int bookingServiceId);
    List<BookingServiceEntity> findByBookingId(int bookingId);
    void save(BookingServiceEntity bookingService);
    void delete(int bookingServiceId);
}

