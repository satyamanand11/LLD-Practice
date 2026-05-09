package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.model.ClientEndpointPlan;
import com.lld.ratelimiter.model.ClientPlan;

import java.util.Optional;

public interface ClientEndpointPlanRepository {

    void save(ClientEndpointPlan clientEndpointPlan);

    Optional<ClientPlan> findPlan(String clientId, String endpoint);
}
