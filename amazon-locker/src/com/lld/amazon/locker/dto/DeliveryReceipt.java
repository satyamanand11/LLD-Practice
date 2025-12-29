package com.lld.amazon.locker.dto;

import java.time.Instant;

public record DeliveryReceipt(String assignmentId,
                              String lockerId,
                              String compartmentId,
                              String customerCode,
                              Instant expiresAt) {}
