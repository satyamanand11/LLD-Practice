package com.lld.amazon.locker.model;

public class Compartment {

    private final String compartmentId;
    private final String lockerId;
    private final LockerSize size;
    private final Dimensions interior;

    private boolean available = true;

    public Compartment(String compartmentId, String lockerId, LockerSize size, Dimensions interior) {
        this.compartmentId = compartmentId;
        this.lockerId = lockerId;
        this.size = size;
        this.interior = interior;
    }

    boolean canFitAndAvailable(Dimensions required) {
        return available && required.fitsInside(interior);
    }

    void reserve() {
        if (!available) throw new IllegalStateException("Compartment already reserved: " + compartmentId);
        available = false;
    }

    void release() {
        available = true;
    }

    public String getCompartmentId() { return compartmentId; }
    public String getLockerId() { return lockerId; }
    public LockerSize getSize() { return size; }
    public Dimensions getInterior() { return interior; }
    public boolean isAvailable() { return available; }
}
