package com.lld.amazon.locker.model;

import java.time.*;

public record OperatingHours(LocalTime open, LocalTime close) {
    public boolean isWithin(LocalDateTime time) {
        LocalTime t = time.toLocalTime();
        return !t.isBefore(open) && !t.isAfter(close);
    }
}
