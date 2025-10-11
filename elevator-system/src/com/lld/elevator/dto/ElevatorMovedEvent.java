package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;

public record ElevatorMovedEvent(int elevatorId, int from, int to, Direction dir) {}
