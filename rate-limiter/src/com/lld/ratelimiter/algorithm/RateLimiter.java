package com.lld.ratelimiter.algorithm;

import com.lld.ratelimiter.model.RateLimitResult;

public interface RateLimiter {

    RateLimitResult allow(String clientId);
}
