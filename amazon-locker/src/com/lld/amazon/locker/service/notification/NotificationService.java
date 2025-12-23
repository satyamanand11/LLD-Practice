package com.lld.amazon.locker.service.notification;

public interface NotificationService {
    void notifyCustomer(String customerId, String message);
    void notifyLogistics(String message);
}
