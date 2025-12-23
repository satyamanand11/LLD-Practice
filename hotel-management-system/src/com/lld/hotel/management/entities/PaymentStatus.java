package com.lld.hotel.management.entities;

public enum PaymentStatus {
    CREATED,        // Payment record created
    AUTHORIZED,     // Payment authorized (optional step)
    CAPTURED,       // Money successfully captured
    REFUNDED,       // Money refunded
    FAILED          // Payment failed
}
