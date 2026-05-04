package com.lld.job.scheduler.handler;

import com.lld.job.scheduler.model.TaskExecutionContext;

public interface TaskHandler {
    void execute(TaskExecutionContext context);
}
