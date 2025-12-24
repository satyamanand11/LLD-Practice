package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Invoice;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<Integer, Invoice> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Invoice> findByBookingId(int bookingId) {
        return store.values()
                .stream()
                .filter(i -> i.getBookingId() == bookingId)
                .findFirst();
    }

    @Override
    public void save(Invoice invoice) {
        store.put(invoice.getInvoiceId(), invoice);
    }
}
