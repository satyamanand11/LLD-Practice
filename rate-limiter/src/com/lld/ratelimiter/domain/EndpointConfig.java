package com.lld.ratelimiter.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for an endpoint's rate limiting.
 * Contains algorithm type and algorithm-specific parameters.
 */
public class EndpointConfig {
    private final String endpoint;
    private final String algorithmType;
    private final Map<String, Object> parameters;

    public EndpointConfig(String endpoint, String algorithmType, Map<String, Object> parameters) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint cannot be null or empty");
        }
        if (algorithmType == null || algorithmType.isBlank()) {
            throw new IllegalArgumentException("algorithmType cannot be null or empty");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("parameters cannot be null");
        }
        this.endpoint = endpoint;
        this.algorithmType = algorithmType;
        this.parameters = new HashMap<>(parameters); // Defensive copy
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters); // Defensive copy
    }

    /**
     * Gets a parameter value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException("Parameter " + key + " is not of type " + type.getSimpleName());
        }
        return (T) value;
    }

    /**
     * Gets a parameter value with default if missing.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type, T defaultValue) {
        T value = getParameter(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointConfig that = (EndpointConfig) o;
        return Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(algorithmType, that.algorithmType) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, algorithmType, parameters);
    }

    @Override
    public String toString() {
        return "EndpointConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", algorithmType='" + algorithmType + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}

