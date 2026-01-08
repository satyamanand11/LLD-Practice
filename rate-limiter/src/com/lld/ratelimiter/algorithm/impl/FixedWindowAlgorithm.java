package com.lld.ratelimiter.algorithm.impl;

import com.lld.ratelimiter.algorithm.AlgorithmState;
import com.lld.ratelimiter.algorithm.RateLimitingAlgorithm;
import com.lld.ratelimiter.domain.EndpointConfig;
import com.lld.ratelimiter.domain.RateLimitResult;

/**
 * Fixed Window rate limiting algorithm.
 * 
 * Parameters:
 * - windowSizeMs: long - Fixed window size in milliseconds
 * - maxRequests: int - Maximum requests per window
 * 
 * Logic:
 * 1. If current time outside window, reset window
 * 2. If count < maxRequests, allow and increment
 * 3. Otherwise, reject and calculate retryAfterMs
 */
public class FixedWindowAlgorithm implements RateLimitingAlgorithm {
    public static final String ALGORITHM_NAME = "FixedWindow";
    public static final String PARAM_WINDOW_SIZE_MS = "windowSizeMs";
    public static final String PARAM_MAX_REQUESTS = "maxRequests";

    @Override
    public RateLimitResult checkLimit(AlgorithmState state, EndpointConfig config) {
        long currentTime = System.currentTimeMillis();
        FixedWindowState windowState;

        if (state == null) {
            windowState = (FixedWindowState) createInitialState(config);
        } else {
            if (!(state instanceof FixedWindowState)) {
                throw new IllegalArgumentException("Invalid state type for FixedWindow algorithm");
            }
            windowState = (FixedWindowState) state;
        }

        // Get parameters
        long windowSizeMs = config.getParameter(PARAM_WINDOW_SIZE_MS, Long.class);
        int maxRequests = config.getParameter(PARAM_MAX_REQUESTS, Integer.class);

        // Check if we're in a new window
        if (currentTime >= windowState.getWindowStartTime() + windowSizeMs) {
            // Reset window
            windowState.setWindowStartTime(currentTime);
            windowState.setCount(0);
        }

        // Check if request is allowed
        int currentCount = windowState.getCount();
        if (currentCount < maxRequests) {
            // Allow request - increment count
            windowState.setCount(currentCount + 1);
            int remaining = maxRequests - (currentCount + 1);
            return RateLimitResult.allowed(remaining);
        } else {
            // Reject request - calculate retry after
            long windowEndTime = windowState.getWindowStartTime() + windowSizeMs;
            long retryAfterMs = windowEndTime - currentTime;
            return RateLimitResult.denied(0, Math.max(0, retryAfterMs));
        }
    }

    @Override
    public AlgorithmState createInitialState(EndpointConfig config) {
        long currentTime = System.currentTimeMillis();
        return new FixedWindowState(0, currentTime);
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }
}

