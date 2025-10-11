package com.lld.elevator.dto;

import com.lld.elevator.enums.Direction;

public record HallCallDTO(int floor, Direction direction) {}
