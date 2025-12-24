package com.lld.hotel.management.pattern.factory;

/**
 * Factory Pattern - Concrete Product
 * Push notification channel
 */
public class PushNotification implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println("🔔 [PUSH] To: " + recipient);
        System.out.println("   Message: " + message);
    }

    @Override
    public String getChannelType() {
        return "PUSH";
    }
}

