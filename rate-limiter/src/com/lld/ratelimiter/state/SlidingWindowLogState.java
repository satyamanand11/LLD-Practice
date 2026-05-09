package com.lld.ratelimiter.state;

import java.util.ArrayDeque;
import java.util.Deque;

public final class SlidingWindowLogState {
    private final Deque<Long> requestTimestamps = new ArrayDeque<>();

    public Deque<Long> getRequestTimestamps() {
        return requestTimestamps;
    }
}
