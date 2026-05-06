package com.lld.job.scheduler.service;

import com.lld.job.scheduler.config.SchedulerConfig;
import com.lld.job.scheduler.model.ScheduledTask;
import com.lld.job.scheduler.model.TaskDefinition;
import com.lld.job.scheduler.repository.TaskDefinitionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TaskSchedulerService {

    private static final Duration IDLE_SLEEP = Duration.ofMillis(100);

    private final TaskDefinitionRepository taskDefinitionRepository;
    private final TaskExecutionService taskExecutionService;
    private final PriorityBlockingQueue<ScheduledTask> scheduledTaskQueue;
    private final ExecutorService workerPool;
    private final Semaphore availableWorkerSlots;

    private volatile boolean running;
    private Thread dispatcherThread;

    public TaskSchedulerService(
            SchedulerConfig schedulerConfig,
            TaskDefinitionRepository taskDefinitionRepository,
            TaskExecutionService taskExecutionService
    ) {
        this.taskDefinitionRepository = taskDefinitionRepository;
        this.taskExecutionService = taskExecutionService;

        this.scheduledTaskQueue = new PriorityBlockingQueue<>(
                11,
                ScheduledTaskComparatorFactory.getComparator(
                        schedulerConfig.getTaskPriorityPolicy()
                )
        );

        this.workerPool = Executors.newFixedThreadPool(
                schedulerConfig.getWorkerThreadCount()
        );

        this.availableWorkerSlots = new Semaphore(
                schedulerConfig.getWorkerThreadCount()
        );
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;

        dispatcherThread = new Thread(this::dispatchLoop, "scheduler-dispatcher");
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }

        running = false;

        if (dispatcherThread != null) {
            dispatcherThread.interrupt();
        }

        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void schedule(TaskDefinition taskDefinition) {
        taskDefinitionRepository.save(taskDefinition);

        Optional<Instant> nextExecutionTime =
                taskDefinition.getSchedulePolicy()
                        .nextExecutionAfter(Instant.now());

        nextExecutionTime.ifPresent(executionTime -> {
            ScheduledTask scheduledTask = new ScheduledTask(
                    taskDefinition.getTaskId(),
                    executionTime
            );

            scheduledTaskQueue.offer(scheduledTask);
        });
    }

    public boolean cancel(String taskId) {
        Optional<TaskDefinition> optionalTaskDefinition =
                taskDefinitionRepository.findById(taskId);

        if (optionalTaskDefinition.isEmpty()) {
            return false;
        }

        TaskDefinition taskDefinition = optionalTaskDefinition.get();
        taskDefinition.cancel();

        // Lazy removal: any queued ScheduledTask for this id will be skipped
        // when the dispatcher picks it up because isActive() will return false.
        scheduledTaskQueue.removeIf(
                scheduled -> scheduled.getTaskDefinitionId().equals(taskId)
        );
        return true;
    }

    public boolean pause(String taskId) {
        Optional<TaskDefinition> optionalTaskDefinition =
                taskDefinitionRepository.findById(taskId);

        if (optionalTaskDefinition.isEmpty()) {
            return false;
        }

        optionalTaskDefinition.get().pause();
        return true;
    }

    public boolean resume(String taskId) {
        Optional<TaskDefinition> optionalTaskDefinition =
                taskDefinitionRepository.findById(taskId);

        if (optionalTaskDefinition.isEmpty()) {
            return false;
        }

        TaskDefinition taskDefinition = optionalTaskDefinition.get();
        taskDefinition.resume();

        if (!taskDefinition.isActive()) {
            return false;
        }

        // Re-arm next execution if we have no pending ScheduledTask for this id.
        boolean hasPending = scheduledTaskQueue.stream()
                .anyMatch(s -> s.getTaskDefinitionId().equals(taskId));

        if (!hasPending) {
            taskDefinition.getSchedulePolicy()
                    .nextExecutionAfter(Instant.now())
                    .ifPresent(nextTime -> scheduledTaskQueue.offer(
                            new ScheduledTask(taskId, nextTime)
                    ));
        }
        return true;
    }

    private void dispatchLoop() {
        while (running) {
            try {
                ScheduledTask head = scheduledTaskQueue.peek();

                if (head == null) {
                    sleep(IDLE_SLEEP);
                    continue;
                }

                Instant now = Instant.now();

                if (head.getExecutionTime().isAfter(now)) {
                    Duration remaining = Duration.between(now, head.getExecutionTime());
                    sleep(min(remaining, IDLE_SLEEP));
                    continue;
                }

                if (!availableWorkerSlots.tryAcquire()) {
                    sleep(IDLE_SLEEP);
                    continue;
                }

                ScheduledTask dueTask = scheduledTaskQueue.poll();

                if (dueTask == null) {
                    availableWorkerSlots.release();
                    continue;
                }

                workerPool.submit(() -> {
                    try {
                        taskExecutionService.execute(dueTask);
                        rescheduleIfRequired(dueTask);
                    } finally {
                        availableWorkerSlots.release();
                    }
                });

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception exception) {
                System.out.println("Dispatcher error: " + exception.getMessage());
            }
        }
    }

    private void rescheduleIfRequired(ScheduledTask completedScheduledTask) {
        Optional<TaskDefinition> optionalTaskDefinition =
                taskDefinitionRepository.findById(
                        completedScheduledTask.getTaskDefinitionId()
                );

        if (optionalTaskDefinition.isEmpty()) {
            return;
        }

        TaskDefinition taskDefinition = optionalTaskDefinition.get();

        if (!taskDefinition.isActive()) {
            return;
        }

        Optional<Instant> nextExecutionTime =
                taskDefinition.getSchedulePolicy()
                        .nextExecutionAfter(
                                completedScheduledTask.getExecutionTime()
                        );

        if (nextExecutionTime.isEmpty()) {
            // No further executions scheduled (e.g. one-time task).
            taskDefinition.markCompleted();
            return;
        }

        ScheduledTask nextScheduledTask = new ScheduledTask(
                taskDefinition.getTaskId(),
                nextExecutionTime.get()
        );
        scheduledTaskQueue.offer(nextScheduledTask);
    }

    private void sleep(Duration duration) throws InterruptedException {
        long millis = Math.max(1L, duration.toMillis());
        Thread.sleep(millis);
    }

    private Duration min(Duration first, Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }
}
