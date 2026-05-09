package com.lld.ratelimiter.config;

public final class SlidingWindowLogParams implements AlgorithmParams {
    private final int maxRequests;

    private final long windowSizeMs;

    public SlidingWindowLogParams(int maxRequests, long windowSizeMs) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        if (windowSizeMs <= 0) {
            throw new IllegalArgumentException("windowSizeMs must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowSizeMs() {
        return windowSizeMs;
    }
}
