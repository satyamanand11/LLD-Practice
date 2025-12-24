package com.lld.hotel.management.pattern.factory;

/**
 * Factory Pattern - Product Interface
 * Base interface for notification channels
 */
public interface NotificationChannel {
    void send(String recipient, String message);
    String getChannelType();
}

