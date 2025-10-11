package com.lld.elevator.events;

import com.lld.elevator.enums.EventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DomainEventBus {
    private final Map<EventType, CopyOnWriteArrayList<Subscriber>> subs = new ConcurrentHashMap<>();

    public void subscribe(EventType type, Subscriber s) {
        subs.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(s);
    }

    public void publish(EventType type, Object payload) {
        var list = subs.get(type);
        if (list != null) for (Subscriber s : list) s.onEvent(type, payload);
    }
}
