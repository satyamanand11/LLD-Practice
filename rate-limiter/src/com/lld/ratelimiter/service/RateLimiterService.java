package com.lld.ratelimiter.service;

import com.lld.ratelimiter.algorithm.RateLimiter;
import com.lld.ratelimiter.config.EndpointDefinition;
import com.lld.ratelimiter.config.EndpointPlanPolicy;
import com.lld.ratelimiter.model.ClientPlan;
import com.lld.ratelimiter.model.RateLimitResult;
import com.lld.ratelimiter.factory.RateLimiterFactory;
import com.lld.ratelimiter.repository.ClientEndpointPlanRepository;
import com.lld.ratelimiter.repository.EndpointDefinitionRepository;
import com.lld.ratelimiter.repository.EndpointPlanPolicyRepository;

public final class RateLimiterService {

    private final EndpointDefinitionRepository endpointDefinitionRepository;
    private final EndpointPlanPolicyRepository endpointPlanPolicyRepository;
    private final ClientEndpointPlanRepository clientEndpointPlanRepository;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterFactory rateLimiterFactory;

    public RateLimiterService(
            EndpointDefinitionRepository endpointDefinitionRepository,
            EndpointPlanPolicyRepository endpointPlanPolicyRepository,
            ClientEndpointPlanRepository clientEndpointPlanRepository,
            RateLimiterRegistry rateLimiterRegistry,
            RateLimiterFactory rateLimiterFactory
    ) {
        this.endpointDefinitionRepository = endpointDefinitionRepository;
        this.endpointPlanPolicyRepository = endpointPlanPolicyRepository;
        this.clientEndpointPlanRepository = clientEndpointPlanRepository;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.rateLimiterFactory = rateLimiterFactory;
    }

    public RateLimitResult allow(String clientId, String endpoint) {
        EndpointDefinition endpointDefinition =
                endpointDefinitionRepository.findByEndpoint(endpoint)
                        .orElseGet(() ->
                                endpointDefinitionRepository
                                        .findByEndpoint("DEFAULT")
                                        .orElseThrow(() -> new IllegalStateException(
                                                "No endpoint config found and DEFAULT endpoint config missing"
                                        ))
                        );

        String effectiveEndpoint = endpointDefinition.getEndpoint();

        ClientPlan plan =
                clientEndpointPlanRepository.findPlan(clientId, endpoint)
                        .orElse(ClientPlan.DEFAULT);

        EndpointPlanPolicy policy =
                endpointPlanPolicyRepository.findByEndpointAndPlan(effectiveEndpoint, plan)
                        .orElseGet(() ->
                                endpointPlanPolicyRepository
                                        .findByEndpointAndPlan(effectiveEndpoint, ClientPlan.DEFAULT)
                                        .orElseThrow(() -> new IllegalStateException(
                                                "No policy found for endpoint=" + effectiveEndpoint
                                                        + " and default policy missing"
                                        ))
                        );

        RateLimiter limiter = rateLimiterRegistry.getOrCreate(
                effectiveEndpoint,
                policy.getPlan(),
                () -> rateLimiterFactory.create(
                        endpointDefinition.getAlgorithmType(),
                        policy.getParams()
                )
        );

        return limiter.allow(clientId);
    }
}