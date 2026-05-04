package com.lld.job.scheduler.schedule;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class FixedIntervalSchedulePolicy implements SchedulePolicy {

    Instant startTime;
    Duration interval;

    public FixedIntervalSchedulePolicy(Instant startTime, Duration interval) {
        this.startTime = startTime;
        this.interval = interval;
    }

    @Override
    public Optional<Instant> nextExecutionAfter(Instant time) {
        if(startTime.isAfter(time))
            return Optional.of(startTime);

        long elapsedMillis = Duration.between(startTime, time).toMillis();
        long intervalMillis = interval.toMillis();
        long intervalsPassed = elapsedMillis / intervalMillis;
        long nextInterval = intervalsPassed + 1;
        Instant nextExecutionTime = startTime.plusMillis(nextInterval * intervalMillis);
        return Optional.of(nextExecutionTime);
    }
}
