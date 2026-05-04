package com.lld.job.scheduler.schedule;

import java.time.Instant;
import java.util.Optional;

public class CronSchedulePolicy implements SchedulePolicy {

    String cronExpression;

    public CronSchedulePolicy(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public Optional<Instant> nextExecutionAfter(Instant time) {
        return Optional.empty();
    }
}
