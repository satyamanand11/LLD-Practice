package com.lld.amazon.locker.dto;

import java.time.Instant;

public record ReturnReceipt(String assignmentId,
                            String lockerId,
                            String compartmentId,
                            String customerDropOffCode,
                            String expiresAtNote,
                            Instant expiresAt) {}
