package com.lld.cricbuzz.repository;

import com.lld.cricbuzz.domain.notifications.Subscription;
import com.lld.cricbuzz.domain.notifications.SubscriptionType;

import java.util.List;

/**
 * Repository interface for Subscriptions
 */
public interface SubscriptionRepository {
    void save(Subscription subscription);
    List<Subscription> findByUserId(String userId);
    List<Subscription> findByEntityIdAndType(String entityId, SubscriptionType type);
    void delete(String subscriptionId);
}

