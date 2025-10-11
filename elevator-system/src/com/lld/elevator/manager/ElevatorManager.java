package com.lld.elevator.manager;


import com.lld.elevator.dto.ElevatorSnapshot;
import com.lld.elevator.entities.Elevator;
import com.lld.elevator.entities.Target;
import com.lld.elevator.enums.Direction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElevatorManager {
    private final Map<Integer, Elevator> elevators = new ConcurrentHashMap<>();

    public ElevatorManager(List<Elevator> list) {
        list.forEach(e -> elevators.put(e.getNumber(), e));
    }

    public List<Elevator> getAll() { return new ArrayList<>(elevators.values()); }

    public void addToTargetQueue(int elevatorNumber, Target target) {
        elevators.get(elevatorNumber).enqueue(target.floorFrom(), target.direction());
    }

    public void removeFromTargetQueue(int elevatorNumber, Target target) {
        elevators.get(elevatorNumber).removeStop(target.floorFrom(), target.direction());
    }

    public List<ElevatorSnapshot> snapshotAll() {
        List<ElevatorSnapshot> snaps = new ArrayList<>();
        for (Elevator e : getAll()) {
            snaps.add(new ElevatorSnapshot(
                    e.getNumber(),
                    e.getCurrentFloor(),
                    e.getDirection(),
                    e.getMode(),
                    e.snapshotUp(),
                    e.snapshotDown()
            ));
        }
        return snaps;
    }

    public void removeForOthers(int servingElevatorNumber, int floor, Direction dir) {
        for (Elevator e : getAll())
            if (e.getNumber() != servingElevatorNumber) e.removeStop(floor, dir);
    }

    public void stepAll() { getAll().forEach(Elevator::stepOne); }
}
