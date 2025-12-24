package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Invoice;

import java.util.Optional;

public interface InvoiceRepository {
    Optional<Invoice> findByBookingId(int bookingId);
    void save(Invoice invoice);
}
