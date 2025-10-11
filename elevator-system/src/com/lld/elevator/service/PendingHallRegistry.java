package com.lld.elevator.service;

import com.lld.elevator.dto.ServedEvent;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.EventType;
import com.lld.elevator.events.DomainEventBus;
import com.lld.elevator.manager.ElevatorManager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PendingHallRegistry {
    private record Key(int floor, Direction dir) {}
    private final Set<Key> pending = ConcurrentHashMap.newKeySet();

    public PendingHallRegistry(DomainEventBus bus, ElevatorManager mgr) {
        bus.subscribe(EventType.SERVED, (type, payload) -> {
            ServedEvent ev = (ServedEvent) payload;
            if (pending.remove(new Key(ev.floor(), ev.dir()))) {
                mgr.removeForOthers(ev.elevatorId(), ev.floor(), ev.dir());
            }
        });
    }

    public void add(int floor, Direction dir) { pending.add(new Key(floor, dir)); }
}
