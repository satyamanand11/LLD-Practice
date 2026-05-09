package com.lld.ratelimiter.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public final class InMemoryRateLimitStateStore<S>
        implements RateLimitStateStore<S> {

    private final ConcurrentMap<String, S> states = new ConcurrentHashMap<>();

    @Override
    public S getOrCreate(String clientId, Supplier<S> initializer) {
        return states.computeIfAbsent(clientId, _ -> initializer.get());
    }

    @Override
    public void remove(String clientId) {
        states.remove(clientId);
    }
}
