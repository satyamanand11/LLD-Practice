package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorMode;

import java.util.List;

public record ElevatorSnapshot(
        int id,
        int floor,
        Direction direction,
        ElevatorMode mode,
        List<Integer> upStops,
        List<Integer> downStops
) {}
