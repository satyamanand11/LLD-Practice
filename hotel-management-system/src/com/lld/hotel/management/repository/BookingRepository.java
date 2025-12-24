package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Booking;
import java.util.function.Consumer;

public interface BookingRepository {

    void executeWithLock(int bookingId, Consumer<Booking> action);

    void create(Booking booking);
}
