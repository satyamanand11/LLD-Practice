package com.lld.ratelimiter.state;

public final class TokenBucketState {
    private double availableTokens;
    private long lastRefillTimestampMs;

    public TokenBucketState(double availableTokens, long lastRefillTimestampMs) {
        this.availableTokens = availableTokens;
        this.lastRefillTimestampMs = lastRefillTimestampMs;
    }

    public double getAvailableTokens() {
        return availableTokens;
    }

    public void setAvailableTokens(double availableTokens) {
        this.availableTokens = availableTokens;
    }

    public long getLastRefillTimestampMs() {
        return lastRefillTimestampMs;
    }

    public void setLastRefillTimestampMs(long lastRefillTimestampMs) {
        this.lastRefillTimestampMs = lastRefillTimestampMs;
    }
}
