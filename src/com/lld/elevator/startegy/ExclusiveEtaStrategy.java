package com.lld.elevator.startegy;

import com.lld.elevator.dto.ElevatorSnapshot;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.enums.ElevatorMode;

import java.util.List;

public class ExclusiveEtaStrategy implements SchedulerStrategy {
    @Override
    public List<Integer> select(int floor, Direction dir, List<ElevatorSnapshot> snaps) {
        int best = -1; double score = Double.MAX_VALUE;
        for (var s : snaps) {
            if (s.mode() != ElevatorMode.NORMAL) continue;
            int dist = Math.abs(s.floor() - floor);
            int stops = s.upStops().size() + s.downStops().size();
            int pen = (s.direction() != Direction.IDLE && s.direction() != dir && dir != Direction.IDLE) ? 2 : 0;
            double sc = dist + 0.5 * stops + 2 * pen;
            if (sc < score) { score = sc; best = s.id(); }
        }
        return best == -1 ? List.of() : List.of(best);
    }
}
