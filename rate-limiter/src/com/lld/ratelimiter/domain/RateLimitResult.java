package com.lld.ratelimiter.domain;

import java.util.Objects;

/**
 * Structured result returned by rate limiter.
 * Contains:
 * - allowed: Whether request is allowed
 * - remaining: Remaining requests/tokens
 * - retryAfterMs: Milliseconds to wait before retry (null if allowed)
 */
public class RateLimitResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs;

    public RateLimitResult(boolean allowed, int remaining, Long retryAfterMs) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterMs = retryAfterMs;
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

    public static RateLimitResult allowed(int remaining) {
        return new RateLimitResult(true, remaining, null);
    }

    public static RateLimitResult denied(int remaining, long retryAfterMs) {
        return new RateLimitResult(false, remaining, retryAfterMs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimitResult that = (RateLimitResult) o;
        return allowed == that.allowed &&
                remaining == that.remaining &&
                Objects.equals(retryAfterMs, that.retryAfterMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowed, remaining, retryAfterMs);
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", remaining=" + remaining +
                ", retryAfterMs=" + retryAfterMs +
                '}';
    }
}

