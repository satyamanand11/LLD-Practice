package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(int paymentId);
    void save(Payment payment);
}
