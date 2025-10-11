package com.lld.elevator.factory;


import com.lld.elevator.entities.Elevator;
import com.lld.elevator.events.DomainEventBus;

import java.util.ArrayList;
import java.util.List;

public class ElevatorFactory {
    public static List<Elevator> create(int count, DomainEventBus bus, int startFloor) {
        List<Elevator> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) list.add(new Elevator(i, startFloor, bus));
        return list;
    }
}
