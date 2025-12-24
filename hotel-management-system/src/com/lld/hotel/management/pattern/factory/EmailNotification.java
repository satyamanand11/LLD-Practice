package com.lld.hotel.management.pattern.factory;

/**
 * Factory Pattern - Concrete Product
 * Email notification channel
 */
public class EmailNotification implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println("📧 [EMAIL] To: " + recipient);
        System.out.println("   Message: " + message);
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}

