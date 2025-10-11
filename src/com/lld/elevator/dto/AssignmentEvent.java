package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;

import java.util.List;

public record AssignmentEvent(int floor, Direction dir, List<Integer> elevatorIds) {}
