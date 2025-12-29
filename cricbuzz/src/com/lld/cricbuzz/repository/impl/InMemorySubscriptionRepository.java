package com.lld.cricbuzz.repository.impl;

import com.lld.cricbuzz.domain.notifications.Subscription;
import com.lld.cricbuzz.domain.notifications.SubscriptionType;
import com.lld.cricbuzz.repository.SubscriptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemorySubscriptionRepository implements SubscriptionRepository {
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> userSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Map<SubscriptionType, List<String>>> entitySubscriptions = new ConcurrentHashMap<>();

    @Override
    public void save(Subscription subscription) {
        subscriptions.put(subscription.getSubscriptionId(), subscription);
        userSubscriptions.computeIfAbsent(subscription.getUserId(), k -> new ArrayList<>())
            .add(subscription.getSubscriptionId());
        entitySubscriptions.computeIfAbsent(subscription.getEntityId(), k -> new ConcurrentHashMap<>())
            .computeIfAbsent(subscription.getType(), k -> new ArrayList<>())
            .add(subscription.getSubscriptionId());
    }

    @Override
    public List<Subscription> findByUserId(String userId) {
        List<String> subscriptionIds = userSubscriptions.getOrDefault(userId, new ArrayList<>());
        return subscriptionIds.stream()
            .map(subscriptions::get)
            .filter(s -> s != null && s.isActive())
            .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> findByEntityIdAndType(String entityId, SubscriptionType type) {
        Map<SubscriptionType, List<String>> typeMap = entitySubscriptions.getOrDefault(entityId, new ConcurrentHashMap<>());
        List<String> subscriptionIds = typeMap.getOrDefault(type, new ArrayList<>());
        return subscriptionIds.stream()
            .map(subscriptions::get)
            .filter(s -> s != null && s.isActive())
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String subscriptionId) {
        Subscription subscription = subscriptions.remove(subscriptionId);
        if (subscription != null) {
            userSubscriptions.getOrDefault(subscription.getUserId(), new ArrayList<>())
                .remove(subscriptionId);
            Map<SubscriptionType, List<String>> typeMap = entitySubscriptions.get(subscription.getEntityId());
            if (typeMap != null) {
                typeMap.getOrDefault(subscription.getType(), new ArrayList<>())
                    .remove(subscriptionId);
            }
        }
    }
}

