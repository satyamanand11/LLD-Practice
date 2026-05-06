import com.lld.job.scheduler.config.SchedulerConfig;
import com.lld.job.scheduler.config.TaskPriorityPolicy;
import com.lld.job.scheduler.facade.JobSchedulerSystem;
import com.lld.job.scheduler.facade.JobSchedulerSystemImpl;
import com.lld.job.scheduler.handler.EmailReportTaskHandler;
import com.lld.job.scheduler.handler.TaskHandler;
import com.lld.job.scheduler.model.TaskExecution;
import com.lld.job.scheduler.schedule.CronSchedulePolicy;
import com.lld.job.scheduler.schedule.FixedIntervalSchedulePolicy;
import com.lld.job.scheduler.schedule.OneTimeSchedulePolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        SchedulerConfig config = new SchedulerConfig(
                4,
                TaskPriorityPolicy.EARLIEST_EXECUTION_TIME_FIRST
        );

        JobSchedulerSystem scheduler = new JobSchedulerSystemImpl(config);
        scheduler.start();

        System.out.println("[main] Job scheduler started");

        // 1. One-time task fired ~2 seconds from now.
        String oneTimeTaskId = scheduler.scheduleTask(
                "one-time-greeting",
                new OneTimeSchedulePolicy(Instant.now().plusSeconds(2)),
                context -> System.out.println(
                        "[one-time] hello at " + Instant.now()
                                + " (exec=" + context.getExecutionId() + ")"
                )
        );
        System.out.println("[main] Scheduled one-time task: " + oneTimeTaskId);

        // 2. Fixed-interval task that fires every 1s for ~5s, then we cancel it.
        String everySecondTaskId = scheduler.scheduleTask(
                "heartbeat",
                new FixedIntervalSchedulePolicy(
                        Instant.now().plusSeconds(1),
                        Duration.ofSeconds(1)
                ),
                context -> System.out.println(
                        "[heartbeat] tick at " + Instant.now()
                )
        );
        System.out.println("[main] Scheduled heartbeat task: " + everySecondTaskId);

        // 3. Cron task: every minute (won't fire in this short demo, just shows wiring).
        String cronTaskId = scheduler.scheduleTask(
                "every-minute-email",
                new CronSchedulePolicy("* * * * *"),
                new EmailReportTaskHandler()
        );
        System.out.println("[main] Scheduled cron task: " + cronTaskId);

        // 4. Burst of 12 quick tasks scheduled at the same time to exercise
        //    the priority policy + worker pool (10 worker requirement).
        scheduleBurst(scheduler, 12);

        // Let things run for a few ticks.
        Thread.sleep(5000);

        // Cancel the heartbeat to demonstrate cancellation.
        boolean cancelled = scheduler.cancelTask(everySecondTaskId);
        System.out.println("[main] Cancelled heartbeat task? " + cancelled);

        // Pause + resume demo on the cron task.
        scheduler.pauseTask(cronTaskId);
        System.out.println("[main] Paused cron task");
        scheduler.resumeTask(cronTaskId);
        System.out.println("[main] Resumed cron task");

        Thread.sleep(2000);

        // Print execution history of the one-time task.
        List<TaskExecution> oneTimeExecutions = scheduler.getExecutions(oneTimeTaskId);
        System.out.println("[main] One-time executions:");
        for (TaskExecution execution : oneTimeExecutions) {
            System.out.println(
                    "  - id=" + execution.getExecutionId()
                            + " status=" + execution.getStatus()
                            + " started=" + execution.getActualStartTime()
                            + " completed=" + execution.getCompletedAt()
            );
        }

        scheduler.stop();
        System.out.println("[main] Job scheduler stopped");
    }

    private static void scheduleBurst(JobSchedulerSystem scheduler, int count) {
        Instant fireAt = Instant.now().plusSeconds(3);
        for (int i = 0; i < count; i++) {
            final int idx = i;
            TaskHandler handler = context -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[burst-" + idx + "] ran at " + Instant.now());
            };
            scheduler.scheduleTask(
                    "burst-task-" + idx,
                    new OneTimeSchedulePolicy(fireAt),
                    handler
            );
        }
    }
}
