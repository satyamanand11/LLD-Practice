package com.lld.amazon.locker.model;

import java.util.*;
import java.util.concurrent.locks.*;

public class Locker {

    private final String lockerId;
    private final Map<String, Compartment> compartments = new HashMap<>();

    // Concurrency: all reserve/release operations are atomic per locker
    private final ReentrantLock lock = new ReentrantLock(true);

    public Locker(String lockerId, List<Compartment> compartments) {
        this.lockerId = lockerId;
        for (Compartment c : compartments) {
            this.compartments.put(c.getCompartmentId(), c);
        }
    }

    public String getLockerId() { return lockerId; }

    public Optional<Compartment> reserveSmallestFit(Dimensions required) {
        lock.lock();
        try {
            return compartments.values().stream()
                    .filter(c -> c.canFitAndAvailable(required))
                    // smallest locker size first; if tie, smallest volume
                    .min(Comparator
                            .comparing((Compartment c) -> c.getSize().ordinal())
                            .thenComparingLong(c -> volume(c.getInterior())))
                    .map(c -> {
                        c.reserve();
                        return c;
                    });
        } finally {
            lock.unlock();
        }
    }

    public void releaseCompartment(String compartmentId) {
        lock.lock();
        try {
            Compartment c = compartments.get(compartmentId);
            if (c == null) throw new IllegalArgumentException("Invalid compartment: " + compartmentId);
            c.release();
        } finally {
            lock.unlock();
        }
    }

    private long volume(Dimensions d) {
        return (long) d.width() * d.height() * d.depth();
    }
}
