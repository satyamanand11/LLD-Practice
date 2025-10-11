package com.lld.elevator.entities;

import com.lld.elevator.enums.Direction;

public record Target(int floorFrom, Direction direction) {
}
