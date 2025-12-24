package com.lld.hotel.management.observer;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
