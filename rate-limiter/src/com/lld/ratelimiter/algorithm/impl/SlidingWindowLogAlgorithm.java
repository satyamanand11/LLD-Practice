package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;

/**
 * Sliding Window Log rate limiting algorithm.
 * 
 * Parameters:
 * - windowSizeMs: long - Time window in milliseconds
 * - maxRequests: int - Maximum requests in window
 * 
 * Logic:
 * 1. Remove timestamps outside window
 * 2. If count < maxRequests, allow and add timestamp
 * 3. Otherwise, reject and calculate retryAfterMs
 */
public class SlidingWindowLogAlgorithm implements RateLimitingAlgorithm {
    public static final String ALGORITHM_NAME = "SlidingWindowLog";
    public static final String PARAM_WINDOW_SIZE_MS = "windowSizeMs";
    public static final String PARAM_MAX_REQUESTS = "maxRequests";

    @Override
    public RateLimitResult checkLimit(AlgorithmState state, EndpointConfig config) {
        long currentTime = System.currentTimeMillis();
        SlidingWindowLogState windowState;

        if (state == null) {
            windowState = (SlidingWindowLogState) createInitialState(config);
        } else {
            if (!(state instanceof SlidingWindowLogState)) {
                throw new IllegalArgumentException("Invalid state type for SlidingWindowLog algorithm");
            }
            windowState = (SlidingWindowLogState) state;
        }

        // Get parameters
        long windowSizeMs = config.getParameter(PARAM_WINDOW_SIZE_MS, Long.class);
        int maxRequests = config.getParameter(PARAM_MAX_REQUESTS, Integer.class);

        // Remove timestamps outside the window
        long windowStart = currentTime - windowSizeMs;
        windowState.removeTimestampsBefore(windowStart);

        // Check if request is allowed
        int currentCount = windowState.getCount();
        if (currentCount < maxRequests) {
            // Allow request - add timestamp
            windowState.addTimestamp(currentTime);
            int remaining = maxRequests - (currentCount + 1);
            return RateLimitResult.allowed(remaining);
        } else {
            // Reject request - calculate retry after
            Long oldestTimestamp = windowState.getOldestTimestamp();
            if (oldestTimestamp != null) {
                long retryAfterMs = (oldestTimestamp + windowSizeMs) - currentTime;
                return RateLimitResult.denied(0, Math.max(0, retryAfterMs));
            } else {
                // Should not happen, but handle gracefully
                return RateLimitResult.denied(0, windowSizeMs);
            }
        }
    }

    @Override
    public AlgorithmState createInitialState(EndpointConfig config) {
        return new SlidingWindowLogState();
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }
}

