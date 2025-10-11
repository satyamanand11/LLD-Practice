package com.lld.elevator.service;

import com.lld.elevator.dto.AssignmentEvent;
import com.lld.elevator.dto.HallCallDTO;
import com.lld.elevator.entities.Target;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.EventType;
import com.lld.elevator.events.DomainEventBus;
import com.lld.elevator.manager.ElevatorManager;
import com.lld.elevator.startegy.BroadcastStrategy;
import com.lld.elevator.startegy.SchedulerStrategy;

public class ElevatorService {
    private final ElevatorManager elevatorManager;
    private final DomainEventBus eventBus;
    private final PendingHallRegistry pendingRegistry;
    private volatile SchedulerStrategy scheduler;

    public ElevatorService(ElevatorManager m, SchedulerStrategy s, DomainEventBus bus) {
        this.elevatorManager = m; this.scheduler = s; this.eventBus = bus;
        this.pendingRegistry = new PendingHallRegistry(bus, m);
    }

    public void setStrategy(SchedulerStrategy strategy) { this.scheduler = strategy; }

    // Hall call (building scope, uses strategy)
    public void requestElevator(int floor, Direction direction) {
        eventBus.publish(EventType.HALL_CALL, new HallCallDTO(floor, direction));
        var snaps = elevatorManager.snapshotAll();
        var targetElevators = scheduler.select(floor, direction, snaps);
        if (targetElevators.isEmpty()) return;

        eventBus.publish(EventType.ASSIGNMENT, new AssignmentEvent(floor, direction, targetElevators));

        if (scheduler instanceof BroadcastStrategy) {
            for (int id : targetElevators) elevatorManager.addToTargetQueue(id, new Target(floor, direction));
            pendingRegistry.add(floor, direction);
        } else {
            int id = targetElevators.get(0);
            elevatorManager.addToTargetQueue(id, new Target(floor, direction));
        }
    }

    // Car (cabin) call — elevator-scoped, no scheduling
    public void requestCarCall(int elevatorId, int floor) {
        elevatorManager.addToTargetQueue(elevatorId, new Target(floor, Direction.IDLE));
    }

    public void stepAll() { elevatorManager.stepAll(); }

    public void printStatus() { elevatorManager.getAll().forEach(e -> System.out.println(e.status())); }
}
