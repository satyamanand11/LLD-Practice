package com.lld.ratelimiter.config;

import com.lld.ratelimiter.model.ClientPlan;

import java.util.Objects;

public final class EndpointPlanPolicy {

    private final String endpoint;
    private final ClientPlan plan;
    private final AlgorithmParams params;

    public EndpointPlanPolicy(String endpoint, ClientPlan plan, AlgorithmParams params) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
        this.plan = Objects.requireNonNull(plan, "plan");
        this.params = Objects.requireNonNull(params, "params");
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ClientPlan getPlan() {
        return plan;
    }

    public AlgorithmParams getParams() {
        return params;
    }
}