package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Payment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Integer, Payment> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Payment> findById(int paymentId) {
        return Optional.ofNullable(store.get(paymentId));
    }

    @Override
    public void save(Payment payment) {
        store.put(payment.getPaymentId(), payment);
    }
}
