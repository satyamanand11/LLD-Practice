package com.lld.ratelimiter.repository;

import java.util.function.Supplier;

public interface RateLimitStateStore<S> {

    S getOrCreate(String clientId, Supplier<S> initializer);

    void remove(String clientId);
}
