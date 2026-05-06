package com.lld.job.scheduler.handler;

import com.lld.job.scheduler.model.TaskExecutionContext;

public class EmailReportTaskHandler implements TaskHandler{

    @Override
    public void execute(TaskExecutionContext context) {
        System.out.println("email sent");
    }
}
