package com.lld.ratelimiter.facade;

import com.lld.ratelimiter.config.EndpointDefinition;
import com.lld.ratelimiter.config.EndpointPlanPolicy;
import com.lld.ratelimiter.model.ClientEndpointPlan;
import com.lld.ratelimiter.model.RateLimitResult;
import com.lld.ratelimiter.factory.RateLimiterFactory;
import com.lld.ratelimiter.service.RateLimiterRegistry;
import com.lld.ratelimiter.repository.ClientEndpointPlanRepository;
import com.lld.ratelimiter.repository.EndpointDefinitionRepository;
import com.lld.ratelimiter.repository.EndpointPlanPolicyRepository;
import com.lld.ratelimiter.repository.InMemoryClientEndpointPlanRepository;
import com.lld.ratelimiter.repository.InMemoryEndpointDefinitionRepository;
import com.lld.ratelimiter.repository.InMemoryEndpointPlanPolicyRepository;
import com.lld.ratelimiter.service.RateLimiterService;

public final class RateLimiterFacade {

    private static final RateLimiterFacade INSTANCE = new RateLimiterFacade();

    private final EndpointDefinitionRepository endpointDefinitionRepository;
    private final EndpointPlanPolicyRepository endpointPlanPolicyRepository;
    private final ClientEndpointPlanRepository clientEndpointPlanRepository;

    private final RateLimiterService rateLimiterService;

    private RateLimiterFacade() {
        this.endpointDefinitionRepository =
                new InMemoryEndpointDefinitionRepository();

        this.endpointPlanPolicyRepository =
                new InMemoryEndpointPlanPolicyRepository();

        this.clientEndpointPlanRepository =
                new InMemoryClientEndpointPlanRepository();

        RateLimiterRegistry rateLimiterRegistry = new RateLimiterRegistry();
        RateLimiterFactory rateLimiterFactory = new RateLimiterFactory();

        this.rateLimiterService = new RateLimiterService(
                endpointDefinitionRepository,
                endpointPlanPolicyRepository,
                clientEndpointPlanRepository,
                rateLimiterRegistry,
                rateLimiterFactory
        );
    }

    public static RateLimiterFacade getInstance() {
        return INSTANCE;
    }

    public void registerEndpoint(EndpointDefinition endpointDefinition) {
        endpointDefinitionRepository.save(endpointDefinition);
    }

    public void registerEndpointPlanPolicy(EndpointPlanPolicy endpointPlanPolicy) {
        endpointPlanPolicyRepository.save(endpointPlanPolicy);
    }

    public void registerClientEndpointPlan(ClientEndpointPlan clientEndpointPlan) {
        clientEndpointPlanRepository.save(clientEndpointPlan);
    }

    public RateLimitResult allow(String clientId, String endpoint) {
        return rateLimiterService.allow(clientId, endpoint);
    }
}


