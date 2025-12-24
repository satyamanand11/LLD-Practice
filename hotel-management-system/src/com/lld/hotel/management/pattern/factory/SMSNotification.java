package com.lld.hotel.management.pattern.factory;

/**
 * Factory Pattern - Concrete Product
 * SMS notification channel
 */
public class SMSNotification implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println("📱 [SMS] To: " + recipient);
        System.out.println("   Message: " + message);
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }
}

