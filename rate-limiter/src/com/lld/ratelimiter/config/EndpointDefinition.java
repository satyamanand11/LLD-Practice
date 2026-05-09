package com.lld.ratelimiter.config;

import com.lld.ratelimiter.model.RateLimitAlgorithmType;

import java.util.Objects;

public final class EndpointDefinition {

    private final String endpoint;
    private final RateLimitAlgorithmType algorithmType;

    public EndpointDefinition(String endpoint, RateLimitAlgorithmType algorithmType) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.algorithmType = Objects.requireNonNull(algorithmType, "algorithmType");
    }

    public String getEndpoint() {
        return endpoint;
    }

    public RateLimitAlgorithmType getAlgorithmType() {
        return algorithmType;
    }
}