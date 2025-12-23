package com.lld.amazon.locker.service.expiry;

import java.time.Instant;

public interface ExpiryService {
    void scheduleExpiry(String assignmentId, Instant expiresAt);
}
