package com.lld.hotel.management.entities;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private IdGenerator() {
    }

    public static int nextId() {
        return SEQ.getAndIncrement();
    }
}
