package com.lld.ratelimiter.repository;


import com.lld.ratelimiter.model.ClientEndpointPlan;
import com.lld.ratelimiter.model.ClientPlan;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryClientEndpointPlanRepository
        implements ClientEndpointPlanRepository {

    private final ConcurrentMap<String, ConcurrentMap<String, ClientPlan>> plans =
            new ConcurrentHashMap<>();

    @Override
    public void save(ClientEndpointPlan clientEndpointPlan) {
        plans
                .computeIfAbsent(
                        clientEndpointPlan.getClientId(),
                        clientId -> new ConcurrentHashMap<>()
                )
                .put(
                        clientEndpointPlan.getEndpoint(),
                        clientEndpointPlan.getClientPlan()
                );
    }

    @Override
    public Optional<ClientPlan> findPlan(String clientId, String endpoint) {
        ConcurrentMap<String, ClientPlan> endpointPlans =
                plans.get(clientId);

        if (endpointPlans == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(endpointPlans.get(endpoint));
    }
}
