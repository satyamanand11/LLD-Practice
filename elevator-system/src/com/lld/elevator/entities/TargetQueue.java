package com.lld.elevator.entities;

import java.util.Optional;
import java.util.PriorityQueue;

public class TargetQueue {
    private final PriorityQueue<Target> targets = new PriorityQueue<>(
            (a,b) -> Integer.compare(a.floorFrom(), b.floorFrom())
    );
    public void add(Target t) { targets.add(t); }
    public void remove(Target t) { targets.remove(t); }
    public Optional<Target> peekNext() { return Optional.ofNullable(targets.peek()); }
    public boolean isEmpty() { return targets.isEmpty(); }
}
