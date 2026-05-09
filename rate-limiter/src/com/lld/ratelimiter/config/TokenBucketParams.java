package com.lld.ratelimiter.config;

public final class TokenBucketParams implements AlgorithmParams {

    private final int capacity;

    private final double refillRatePerSecond;

    public TokenBucketParams(int capacity, double refillRatePerSecond) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException("refillRatePerSecond must be positive");
        }
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

}
