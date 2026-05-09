package com.lld.ratelimiter.repository;

import com.lld.ratelimiter.config.EndpointDefinition;

import java.util.Optional;

public interface EndpointDefinitionRepository {

    void save(EndpointDefinition endpointDefinition);

    Optional<EndpointDefinition> findByEndpoint(String endpoint);
}
