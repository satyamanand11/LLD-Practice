package com.lld.cricbuzz.service;

import com.lld.cricbuzz.domain.notifications.NotificationEvent;
import com.lld.cricbuzz.domain.notifications.NotificationType;
import com.lld.cricbuzz.domain.notifications.Subscription;
import com.lld.cricbuzz.domain.notifications.SubscriptionType;
import com.lld.cricbuzz.events.*;
import com.lld.cricbuzz.repository.SubscriptionRepository;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing notifications and subscriptions
 * Observer Pattern: Handles event-driven notifications
 */
public class NotificationService implements EventHandler {
    private final SubscriptionRepository subscriptionRepository;

    public NotificationService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription subscribe(String userId, SubscriptionType type, String entityId) {
        String subscriptionId = "SUB_" + UUID.randomUUID().toString().substring(0, 8);
        Subscription subscription = new Subscription(subscriptionId, userId, type, entityId);
        subscriptionRepository.save(subscription);
        return subscription;
    }

    public void unsubscribe(String subscriptionId) {
        subscriptionRepository.delete(subscriptionId);
    }

    public List<Subscription> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof MatchEvent;
    }

    @Override
    public void handle(DomainEvent event) {
        if (event instanceof WicketEvent) {
            handleWicketEvent((WicketEvent) event);
        } else if (event instanceof BoundaryEvent) {
            handleBoundaryEvent((BoundaryEvent) event);
        } else if (event instanceof MilestoneEvent) {
            handleMilestoneEvent((MilestoneEvent) event);
        } else if (event instanceof MatchStartEvent) {
            handleMatchStartEvent((MatchStartEvent) event);
        } else if (event instanceof MatchEndEvent) {
            handleMatchEndEvent((MatchEndEvent) event);
        }
    }

    private void handleWicketEvent(WicketEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
            .findByEntityIdAndType(event.getMatchId(), SubscriptionType.MATCH);
        String message = "Wicket! " + event.getWicket().getBatterId() + " dismissed";
        sendNotifications(subscriptions, event.getMatchId(), NotificationType.WICKET, message);
    }

    private void handleBoundaryEvent(BoundaryEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
            .findByEntityIdAndType(event.getMatchId(), SubscriptionType.MATCH);
        NotificationType type = event.isSix() ? NotificationType.SIX : NotificationType.FOUR;
        String message = event.getRuns() + " runs! " + event.getBatterId();
        sendNotifications(subscriptions, event.getMatchId(), type, message);
        
        // Also notify player-specific subscriptions
        List<Subscription> playerSubs = subscriptionRepository
            .findByEntityIdAndType(event.getBatterId(), SubscriptionType.PLAYER);
        sendNotifications(playerSubs, event.getMatchId(), type, message);
    }

    private void handleMilestoneEvent(MilestoneEvent event) {
        List<Subscription> matchSubs = subscriptionRepository
            .findByEntityIdAndType(event.getMatchId(), SubscriptionType.MATCH);
        List<Subscription> playerSubs = subscriptionRepository
            .findByEntityIdAndType(event.getPlayerId(), SubscriptionType.PLAYER);
        
        NotificationType notificationType = mapMilestoneToNotificationType(event.getType());
        String message = "Milestone! " + event.getPlayerId() + " reached " + event.getValue();
        
        sendNotifications(matchSubs, event.getMatchId(), notificationType, message);
        sendNotifications(playerSubs, event.getMatchId(), notificationType, message);
    }

    private void handleMatchStartEvent(MatchStartEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
            .findByEntityIdAndType(event.getMatchId(), SubscriptionType.MATCH);
        sendNotifications(subscriptions, event.getMatchId(), NotificationType.MATCH_START,
            "Match has started!");
    }

    private void handleMatchEndEvent(MatchEndEvent event) {
        List<Subscription> subscriptions = subscriptionRepository
            .findByEntityIdAndType(event.getMatchId(), SubscriptionType.MATCH);
        String message = "Match ended! Winner: " + event.getWinnerTeamId();
        sendNotifications(subscriptions, event.getMatchId(), NotificationType.MATCH_END, message);
    }

    private NotificationType mapMilestoneToNotificationType(MilestoneEvent.MilestoneType type) {
        return switch (type) {
            case FIFTY -> NotificationType.MILESTONE_50;
            case HUNDRED -> NotificationType.MILESTONE_100;
            case HUNDRED_FIFTY -> NotificationType.MILESTONE_150;
            case DOUBLE_HUNDRED -> NotificationType.MILESTONE_200;
            case FIVE_WICKET_HAUL -> NotificationType.FIVE_WICKET_HAUL;
            case HAT_TRICK -> NotificationType.HAT_TRICK;
        };
    }

    private void sendNotifications(List<Subscription> subscriptions, String matchId,
                                  NotificationType type, String message) {
        for (Subscription subscription : subscriptions) {
            String eventId = "NOTIF_" + UUID.randomUUID().toString().substring(0, 8);
            NotificationEvent notification = new NotificationEvent(eventId, type, matchId,
                subscription.getEntityId(), message);
            // In a real system, this would send via email/SMS/push notification
            System.out.println("Notification to user " + subscription.getUserId() + ": " + message);
        }
    }
}

