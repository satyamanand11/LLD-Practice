package com.lld.job.scheduler.schedule;

import java.time.Instant;
import java.util.Optional;

public interface SchedulePolicy {
    Optional<Instant> nextExecutionAfter(Instant time);
}
