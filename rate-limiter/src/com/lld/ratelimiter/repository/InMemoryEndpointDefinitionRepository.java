package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.config.EndpointDefinition;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryEndpointDefinitionRepository
        implements EndpointDefinitionRepository {

    private final ConcurrentMap<String, EndpointDefinition> endpointDefinitions = new ConcurrentHashMap<>();

    @Override
    public void save(EndpointDefinition endpointDefinition) {
        endpointDefinitions.put(endpointDefinition.getEndpoint(), endpointDefinition
        );
    }

    @Override
    public Optional<EndpointDefinition> findByEndpoint(String endpoint) {
        return Optional.ofNullable(endpointDefinitions.get(endpoint));
    }
}
