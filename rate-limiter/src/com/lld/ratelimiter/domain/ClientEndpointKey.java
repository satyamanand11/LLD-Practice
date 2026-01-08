package com.lld.ratelimiter.domain;

import java.util.Objects;

/**
 * Immutable value object representing the unique identity of rate limit state.
 * Composite key: (clientId, endpoint)
 * Used for:
 * - Map keys in repository
 * - State identification
 */
public class ClientEndpointKey implements Comparable<ClientEndpointKey> {
    private final String clientId;
    private final String endpoint;

    public ClientEndpointKey(String clientId, String endpoint) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint cannot be null or empty");
        }
        this.clientId = clientId;
        this.endpoint = endpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientEndpointKey that = (ClientEndpointKey) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, endpoint);
    }

    /**
     * Implements Comparable for deterministic ordering.
     */
    @Override
    public int compareTo(ClientEndpointKey other) {
        int clientCompare = this.clientId.compareTo(other.clientId);
        if (clientCompare != 0) {
            return clientCompare;
        }
        return this.endpoint.compareTo(other.endpoint);
    }

    @Override
    public String toString() {
        return "ClientEndpointKey{" +
                "clientId='" + clientId + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}

