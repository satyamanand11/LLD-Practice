package com.lld.elevator.startegy;

import com.lld.elevator.dto.ElevatorSnapshot;
import com.lld.elevator.enums.Direction;

import java.util.List;

public interface SchedulerStrategy {
    List<Integer> select(int floor, Direction direction, List<ElevatorSnapshot> elevators);
}
