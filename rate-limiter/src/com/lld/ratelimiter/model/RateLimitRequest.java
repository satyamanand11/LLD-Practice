package com.lld.ratelimiter.model;

public final class RateLimitRequest {
    private final String clientId;
    private final String endpoint;

    public RateLimitRequest(String clientId, String endpoint) {
        this.clientId = clientId;
        this.endpoint = endpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
