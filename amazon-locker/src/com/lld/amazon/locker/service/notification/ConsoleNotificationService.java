package com.lld.amazon.locker.service.notification;

public class ConsoleNotificationService implements NotificationService {

    @Override
    public void notifyCustomer(String customerId, String message) {
        System.out.println("[CUSTOMER][" + customerId + "] " + message);
    }

    @Override
    public void notifyLogistics(String message) {
        System.out.println("[LOGISTICS] " + message);
    }
}
