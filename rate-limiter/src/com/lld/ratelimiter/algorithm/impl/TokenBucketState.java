package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;

/**
 * State for TokenBucket algorithm.
 */
public class TokenBucketState implements AlgorithmState {
    private double tokens;
    private long lastRefillTime;

    public TokenBucketState(double tokens, long lastRefillTime) {
        this.tokens = tokens;
        this.lastRefillTime = lastRefillTime;
    }

    public double getTokens() {
        return tokens;
    }

    public void setTokens(double tokens) {
        this.tokens = tokens;
    }

    public long getLastRefillTime() {
        return lastRefillTime;
    }

    public void setLastRefillTime(long lastRefillTime) {
        this.lastRefillTime = lastRefillTime;
    }

    @Override
    public String getAlgorithmType() {
        return "TokenBucket";
    }

    @Override
    public String toString() {
        return "TokenBucketState{" +
                "tokens=" + tokens +
                ", lastRefillTime=" + lastRefillTime +
                '}';
    }
}

