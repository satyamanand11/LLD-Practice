package com.lld.bms.repo;

import com.lld.bms.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends Repository<Booking, String> {
    Optional<Booking> findByConfirmationId(String confirmationId);
    List<Booking> findByUserId(String userId);
}

