package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.config.EndpointPlanPolicy;
import com.lld.ratelimiter.model.ClientPlan;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryEndpointPlanPolicyRepository implements EndpointPlanPolicyRepository {

    private final ConcurrentMap<String, ConcurrentMap<ClientPlan, EndpointPlanPolicy>> policies =
            new ConcurrentHashMap<>();

    @Override
    public void save(EndpointPlanPolicy policy) {
        policies
                .computeIfAbsent(policy.getEndpoint(), endpoint -> new ConcurrentHashMap<>())
                .put(policy.getPlan(), policy);
    }

    @Override
    public Optional<EndpointPlanPolicy> findByEndpointAndPlan(
            String endpoint,
            ClientPlan plan
    ) {
        ConcurrentMap<ClientPlan, EndpointPlanPolicy> planPolicies =
                policies.get(endpoint);

        if (planPolicies == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(planPolicies.get(plan));
    }
}
