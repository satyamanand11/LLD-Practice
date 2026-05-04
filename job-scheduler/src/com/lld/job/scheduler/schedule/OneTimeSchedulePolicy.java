package com.lld.job.scheduler.schedule;

import java.time.Instant;
import java.util.Optional;

public class OneTimeSchedulePolicy implements SchedulePolicy {

    Instant executionTime;
    public OneTimeSchedulePolicy(Instant executionTime) {
        this.executionTime = executionTime;
    }
    @Override
    public Optional<Instant> nextExecutionAfter(Instant time) {
        if(executionTime.isAfter(time))
            return Optional.of(executionTime);
        return Optional.empty();
    }
}
