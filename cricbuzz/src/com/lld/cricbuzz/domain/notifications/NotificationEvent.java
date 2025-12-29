package com.lld.cricbuzz.domain.notifications;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Value Object representing a notification event
 */
public class NotificationEvent {
    private final String eventId;
    private final NotificationType type;
    private final String matchId;
    private final String entityId; // Can be playerId, teamId, etc.
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;

    public NotificationEvent(String eventId, NotificationType type, String matchId,
                           String entityId, String message) {
        if (eventId == null || type == null || message == null) {
            throw new IllegalArgumentException("Event ID, type, and message are required");
        }
        this.eventId = eventId;
        this.type = type;
        this.matchId = matchId;
        this.entityId = entityId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
}

