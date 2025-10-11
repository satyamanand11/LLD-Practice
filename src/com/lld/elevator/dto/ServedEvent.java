package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;

public record ServedEvent(int elevatorId, int floor, Direction dir) {}
