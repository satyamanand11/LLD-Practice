package com.lld.cricbuzz.domain.notifications;

/**
 * Entity representing a user subscription
 */
public class Subscription {
    private final String subscriptionId;
    private final String userId;
    private final SubscriptionType type;
    private final String entityId; // matchId, tournamentId, teamId, or playerId
    private boolean isActive;

    public Subscription(String subscriptionId, String userId, SubscriptionType type, String entityId) {
        if (subscriptionId == null || userId == null || type == null || entityId == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.type = type;
        this.entityId = entityId;
        this.isActive = true;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Getters
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getUserId() {
        return userId;
    }

    public SubscriptionType getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isActive() {
        return isActive;
    }
}

