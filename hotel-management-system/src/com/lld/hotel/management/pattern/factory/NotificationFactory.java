package com.lld.hotel.management.pattern.factory;

/**
 * Factory Pattern - Factory
 * Creates appropriate notification channel based on type
 */
public class NotificationFactory {
    public enum NotificationType {
        EMAIL,
        SMS,
        PUSH
    }

    public static NotificationChannel createNotification(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("notification type is required");
        }

        switch (type) {
            case EMAIL:
                return new EmailNotification();
            case SMS:
                return new SMSNotification();
            case PUSH:
                return new PushNotification();
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }

    public static NotificationChannel createNotification(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("notification type is required");
        }
        try {
            return createNotification(NotificationType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }
}

