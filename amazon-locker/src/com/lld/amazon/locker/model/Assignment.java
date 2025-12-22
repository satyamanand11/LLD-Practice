package com.lld.amazon.locker.model;

import java.time.*;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class Assignment {

    private final String assignmentId;
    private final AssignmentType type;

    private final String orderOrReturnId;
    private final String customerId;

    private final String locationId;
    private final String lockerId;
    private final String compartmentId;

    private final String customerCode;   // delivery pickup OR return drop-off
    private final String logisticsCode;  // only for RETURN (R12)
    private final Instant expiresAt;     // 3 days (R6)

    private AssignmentStatus status;

    // Concurrency: pickup vs expiry vs logistics pickup races
    private final ReentrantLock stateLock = new ReentrantLock(true);

    private Assignment(String assignmentId,
                       AssignmentType type,
                       String orderOrReturnId,
                       String customerId,
                       String locationId,
                       String lockerId,
                       String compartmentId,
                       String customerCode,
                       String logisticsCode,
                       Instant expiresAt,
                       AssignmentStatus status) {

        this.assignmentId = assignmentId;
        this.type = type;
        this.orderOrReturnId = orderOrReturnId;
        this.customerId = customerId;
        this.locationId = locationId;
        this.lockerId = lockerId;
        this.compartmentId = compartmentId;
        this.customerCode = customerCode;
        this.logisticsCode = logisticsCode;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static Assignment createDelivery(String orderId,
                                            String customerId,
                                            String locationId,
                                            String lockerId,
                                            String compartmentId,
                                            String customerCode,
                                            Instant expiresAt) {
        return new Assignment(UUID.randomUUID().toString(), AssignmentType.DELIVERY,
                orderId, customerId, locationId, lockerId, compartmentId,
                customerCode, null, expiresAt, AssignmentStatus.ACTIVE);
    }

    public static Assignment createReturn(String returnId,
                                          String customerId,
                                          String locationId,
                                          String lockerId,
                                          String compartmentId,
                                          String customerCode,
                                          String logisticsCode,
                                          Instant expiresAt) {
        return new Assignment(UUID.randomUUID().toString(), AssignmentType.RETURN,
                returnId, customerId, locationId, lockerId, compartmentId,
                customerCode, logisticsCode, expiresAt, AssignmentStatus.ACTIVE);
    }

    public void validateCustomerPickup(String code, Clock clock) {
        stateLock.lock();
        try {
            if (status != AssignmentStatus.ACTIVE) throw new IllegalStateException("Not active");
            if (Instant.now(clock).isAfter(expiresAt)) throw new IllegalStateException("Expired");
            if (!customerCode.equals(code)) throw new SecurityException("Invalid customer code");
        } finally {
            stateLock.unlock();
        }
    }

    public void validateLogisticsPickup(String code, Clock clock) {
        stateLock.lock();
        try {
            if (type != AssignmentType.RETURN) throw new IllegalStateException("Not a return");
            if (status != AssignmentStatus.ACTIVE) throw new IllegalStateException("Not active");
            if (Instant.now(clock).isAfter(expiresAt)) throw new IllegalStateException("Expired");
            if (logisticsCode == null || !logisticsCode.equals(code)) throw new SecurityException("Invalid logistics code");
        } finally {
            stateLock.unlock();
        }
    }

    public boolean tryExpire(Clock clock) {
        stateLock.lock();
        try {
            if (status != AssignmentStatus.ACTIVE) return false;
            if (Instant.now(clock).isBefore(expiresAt)) return false;
            status = AssignmentStatus.EXPIRED;
            return true;
        } finally {
            stateLock.unlock();
        }
    }

    public void markCollected() {
        stateLock.lock();
        try {
            if (status != AssignmentStatus.ACTIVE) throw new IllegalStateException("Cannot collect; status=" + status);
            status = AssignmentStatus.COLLECTED; // R10: code invalidated by status change
        } finally {
            stateLock.unlock();
        }
    }

    public String getAssignmentId() { return assignmentId; }
    public AssignmentType getType() { return type; }
    public String getOrderOrReturnId() { return orderOrReturnId; }
    public String getCustomerId() { return customerId; }
    public String getLocationId() { return locationId; }
    public String getLockerId() { return lockerId; }
    public String getCompartmentId() { return compartmentId; }
    public String getCustomerCode() { return customerCode; }
    public String getLogisticsCode() { return logisticsCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public AssignmentStatus getStatus() { return status; }
}
