package com.lld.ratelimiter.model;

public final class RateLimitResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs;

    private RateLimitResult(boolean allowed, int remaining, Long retryAfterMs) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterMs = retryAfterMs;
    }

    public static RateLimitResult allowed(int remaining) {
        return new RateLimitResult(true, remaining, null);
    }

    public static RateLimitResult denied(long retryAfterMs) {
        return new RateLimitResult(false, 0, retryAfterMs);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getRemaining() {
        return remaining;
    }

    public Long getRetryAfterMs() {
        return retryAfterMs;
    }
}