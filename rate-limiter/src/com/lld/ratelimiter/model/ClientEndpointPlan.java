package com.lld.ratelimiter.model;

public final class ClientEndpointPlan {
    private final String clientId;
    private final String endpoint;
    private final ClientPlan clientPlan;

    public ClientEndpointPlan(String clientId, String endpoint, ClientPlan clientPlan) {
        this.clientId = clientId;
        this.endpoint = endpoint;
        this.clientPlan = clientPlan;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ClientPlan getClientPlan() {
        return clientPlan;
    }
}
