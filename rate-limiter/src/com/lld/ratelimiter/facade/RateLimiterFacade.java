package com.lld.ratelimiter.facade;

import com.lld.ratelimiter.domain.RateLimitResult;

/**
 * Public API of the rate limiter system.
 * Entry point for clients.
 */
public interface RateLimiterFacade {
    /**
     * Checks if a request is allowed and returns rate limit information.
     * 
     * @param clientId Client identifier
     * @param endpoint Endpoint path
     * @return RateLimitResult with allowed status, remaining count, and retryAfterMs
     */
    RateLimitResult checkLimit(String clientId, String endpoint);
}

