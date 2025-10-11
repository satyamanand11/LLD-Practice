package com.lld.elevator.startegy;

import com.lld.elevator.dto.ElevatorSnapshot;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorMode;

import java.util.ArrayList;
import java.util.List;

public class BroadcastStrategy implements SchedulerStrategy {
    @Override
    public List<Integer> select(int floor, Direction direction, List<ElevatorSnapshot> snaps) {
        List<Integer> ids = new ArrayList<>();
        for (var s : snaps) if (s.mode() == ElevatorMode.NORMAL) ids.add(s.id());
        return ids;
    }
}
