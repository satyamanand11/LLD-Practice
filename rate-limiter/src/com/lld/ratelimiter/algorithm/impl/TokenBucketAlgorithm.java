package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;

/**
 * Token Bucket rate limiting algorithm.
 * 
 * Parameters:
 * - capacity: int - Maximum tokens
 * - refillRatePerSecond: double - Tokens added per second
 * 
 * Logic:
 * 1. Refill tokens based on time elapsed
 * 2. If tokens >= 1, allow request and decrement
 * 3. Otherwise, reject and calculate retryAfterMs
 */
public class TokenBucketAlgorithm implements RateLimitingAlgorithm {
    public static final String ALGORITHM_NAME = "TokenBucket";
    public static final String PARAM_CAPACITY = "capacity";
    public static final String PARAM_REFILL_RATE = "refillRatePerSecond";

    @Override
    public RateLimitResult checkLimit(AlgorithmState state, EndpointConfig config) {
        long currentTime = System.currentTimeMillis();
        TokenBucketState bucketState;

        if (state == null) {
            // First request - create initial state
            bucketState = (TokenBucketState) createInitialState(config);
        } else {
            if (!(state instanceof TokenBucketState)) {
                throw new IllegalArgumentException("Invalid state type for TokenBucket algorithm");
            }
            bucketState = (TokenBucketState) state;
        }

        // Get parameters
        int capacity = config.getParameter(PARAM_CAPACITY, Integer.class);
        double refillRatePerSecond = config.getParameter(PARAM_REFILL_RATE, Double.class);

        // Refill tokens based on time elapsed
        long timeElapsed = currentTime - bucketState.getLastRefillTime();
        double tokensToAdd = (timeElapsed / 1000.0) * refillRatePerSecond;
        double newTokens = Math.min(capacity, bucketState.getTokens() + tokensToAdd);
        bucketState.setTokens(newTokens);
        bucketState.setLastRefillTime(currentTime);

        // Check if request is allowed
        if (bucketState.getTokens() >= 1.0) {
            // Allow request - consume one token
            bucketState.setTokens(bucketState.getTokens() - 1.0);
            int remaining = (int) Math.floor(bucketState.getTokens());
            return RateLimitResult.allowed(remaining);
        } else {
            // Reject request - calculate retry after
            double tokensNeeded = 1.0 - bucketState.getTokens();
            double secondsToWait = tokensNeeded / refillRatePerSecond;
            long retryAfterMs = (long) Math.ceil(secondsToWait * 1000);
            int remaining = (int) Math.floor(bucketState.getTokens());
            return RateLimitResult.denied(remaining, retryAfterMs);
        }
    }

    @Override
    public AlgorithmState createInitialState(EndpointConfig config) {
        int capacity = config.getParameter(PARAM_CAPACITY, Integer.class);
        long currentTime = System.currentTimeMillis();
        return new TokenBucketState(capacity, currentTime);
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }
}

