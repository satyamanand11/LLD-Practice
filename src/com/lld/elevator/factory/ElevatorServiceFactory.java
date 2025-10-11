package com.lld.elevator.factory;


import com.lld.elevator.events.DomainEventBus;
import com.lld.elevator.manager.ElevatorManager;
import com.lld.elevator.service.ElevatorService;
import com.lld.elevator.startegy.SchedulerStrategy;

public class ElevatorServiceFactory {
    public static ElevatorService create(ElevatorManager mgr, SchedulerStrategy strategy, DomainEventBus bus) {
        return new ElevatorService(mgr, strategy, bus);
    }
}
