package com.lld.ratelimiter.repository;


import com.lld.ratelimiter.config.EndpointPlanPolicy;
import com.lld.ratelimiter.model.ClientPlan;

import java.util.Optional;

public interface EndpointPlanPolicyRepository {

    void save(EndpointPlanPolicy policy);

    Optional<EndpointPlanPolicy> findByEndpointAndPlan(String endpoint, ClientPlan plan);
}
