package com.lld.ratelimiter.config;

import com.lld.ratelimiter.domain.EndpointConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing endpoint configurations.
 * Configuration is loaded at startup and is immutable.
 */
public class ConfigurationService {
    private final Map<String, EndpointConfig> endpointConfigs = new HashMap<>();
    private EndpointConfig defaultConfig;

    /**
     * Loads configuration from a map.
     * Typically called at startup.
     * 
     * @param configs Map of endpoint -> EndpointConfig
     * @param defaultConfig Default configuration for unconfigured endpoints
     */
    public void loadConfiguration(Map<String, EndpointConfig> configs, EndpointConfig defaultConfig) {
        if (configs == null) {
            throw new IllegalArgumentException("configs cannot be null");
        }
        if (defaultConfig == null) {
            throw new IllegalArgumentException("defaultConfig cannot be null");
        }
        this.endpointConfigs.clear();
        this.endpointConfigs.putAll(configs);
        this.defaultConfig = defaultConfig;
    }

    /**
     * Gets configuration for an endpoint.
     * Returns default configuration if endpoint is not configured.
     * 
     * @param endpoint Endpoint path
     * @return EndpointConfig (never null)
     */
    public EndpointConfig getEndpointConfig(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint cannot be null or empty");
        }
        return endpointConfigs.getOrDefault(endpoint, defaultConfig);
    }

    /**
     * Checks if an endpoint has specific configuration.
     */
    public boolean hasEndpointConfig(String endpoint) {
        return endpointConfigs.containsKey(endpoint);
    }

    /**
     * Gets the default configuration.
     */
    public EndpointConfig getDefaultConfig() {
        return defaultConfig;
    }
}

