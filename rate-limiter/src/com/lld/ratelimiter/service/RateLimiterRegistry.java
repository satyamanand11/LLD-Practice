package com.lld.ratelimiter.service;

import com.lld.ratelimiter.algorithm.RateLimiter;
import com.lld.ratelimiter.model.ClientPlan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public final class RateLimiterRegistry {

    private final ConcurrentMap<String, ConcurrentMap<ClientPlan, RateLimiter>> limiters =
            new ConcurrentHashMap<>();

    public RateLimiter getOrCreate(
            String endpoint,
            ClientPlan plan,
            Supplier<RateLimiter> creator
    ) {
        return limiters
                .computeIfAbsent(endpoint, _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(plan, _ -> creator.get());
    }
}
