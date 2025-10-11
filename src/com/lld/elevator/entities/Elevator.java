package com.lld.elevator.entities;

import com.lld.elevator.dto.ElevatorMovedEvent;
import com.lld.elevator.dto.ServedEvent;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorMode;
import com.lld.elevator.enums.EventType;
import com.lld.elevator.events.DomainEventBus;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator {
    private final int number;
    private int currentFloor;
    private Direction direction = Direction.IDLE;
    private ElevatorMode mode = ElevatorMode.NORMAL;

    private final PriorityQueue<Integer> upStops = new PriorityQueue<>();
    private final PriorityQueue<Integer> downStops = new PriorityQueue<>(Comparator.reverseOrder());
    private final ReentrantLock lock = new ReentrantLock();
    private final DomainEventBus bus;

    public Elevator(int number, int startFloor, DomainEventBus bus) {
        this.number = number; this.currentFloor = startFloor; this.bus = bus;
    }

    public int getNumber() { return number; }
    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public ElevatorMode getMode() { return mode; }

    public List<Integer> snapshotUp() { lock.lock(); try { return new ArrayList<>(upStops); } finally { lock.unlock(); } }
    public List<Integer> snapshotDown() { lock.lock(); try { return new ArrayList<>(downStops); } finally { lock.unlock(); } }

    public void enqueue(int floor, Direction dir) {
        lock.lock();
        try { if (dir == Direction.UP) upStops.add(floor); else downStops.add(floor); }
        finally { lock.unlock(); }
    }

    public void removeStop(int floor, Direction dir) {
        lock.lock();
        try { if (dir == Direction.UP) upStops.remove(floor); else downStops.remove(floor); }
        finally { lock.unlock(); }
    }

    public void stepOne() {
        lock.lock();
        try {
            int before = currentFloor;
            if (!upStops.isEmpty()) {
                direction = Direction.UP;
                currentFloor++;
                bus.publish(EventType.ELEVATOR_MOVED, new ElevatorMovedEvent(number, before, currentFloor, direction));
                if (!upStops.isEmpty() && currentFloor == upStops.peek()) {
                    int f = upStops.poll();
                    bus.publish(EventType.SERVED, new ServedEvent(number, f, Direction.UP));
                }
            } else if (!downStops.isEmpty()) {
                direction = Direction.DOWN;
                currentFloor--;
                bus.publish(EventType.ELEVATOR_MOVED, new ElevatorMovedEvent(number, before, currentFloor, direction));
                if (!downStops.isEmpty() && currentFloor == downStops.peek()) {
                    int f = downStops.poll();
                    bus.publish(EventType.SERVED, new ServedEvent(number, f, Direction.DOWN));
                }
            } else {
                direction = Direction.IDLE;
            }
        } finally { lock.unlock(); }
    }

    public String status() {
        lock.lock();
        try { return "Elevator " + number + " @" + currentFloor + " dir=" + direction + " upQ=" + upStops + " downQ=" + downStops; }
        finally { lock.unlock(); }
    }
}
