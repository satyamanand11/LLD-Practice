package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;

import java.util.ArrayList;
import java.util.List;

/**
 * State for SlidingWindowLog algorithm.
 * Stores list of request timestamps within the window.
 */
public class SlidingWindowLogState implements AlgorithmState {
    private final List<Long> requestTimestamps;

    public SlidingWindowLogState() {
        this.requestTimestamps = new ArrayList<>();
    }

    public SlidingWindowLogState(List<Long> requestTimestamps) {
        this.requestTimestamps = new ArrayList<>(requestTimestamps);
    }

    public List<Long> getRequestTimestamps() {
        return new ArrayList<>(requestTimestamps); // Defensive copy
    }

    public void addTimestamp(long timestamp) {
        requestTimestamps.add(timestamp);
    }

    public void removeTimestampsBefore(long cutoffTime) {
        requestTimestamps.removeIf(ts -> ts < cutoffTime);
    }

    public int getCount() {
        return requestTimestamps.size();
    }

    public Long getOldestTimestamp() {
        return requestTimestamps.isEmpty() ? null : requestTimestamps.get(0);
    }

    @Override
    public String getAlgorithmType() {
        return "SlidingWindowLog";
    }

    @Override
    public String toString() {
        return "SlidingWindowLogState{" +
                "requestCount=" + requestTimestamps.size() +
                '}';
    }
}

