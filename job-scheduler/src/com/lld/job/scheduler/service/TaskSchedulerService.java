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

public class TaskSchedulerService {

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

    public void start() {
        if (running) {
            return;
        }

        running = true;

        dispatcherThread = new Thread(this::dispatchLoop, "scheduler-dispatcher");
        dispatcherThread.start();
    }

    public void stop() {
        running = false;

        if (dispatcherThread != null) {
            dispatcherThread.interrupt();
        }

        workerPool.shutdown();
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

    private void dispatchLoop() {
        while (running) {
            try {
                ScheduledTask scheduledTask = scheduledTaskQueue.peek();

                if (scheduledTask == null) {
                    sleep(Duration.ofMillis(100));
                    continue;
                }

                Instant now = Instant.now();

                if (scheduledTask.getExecutionTime().isAfter(now)) {
                    Duration sleepDuration = Duration.between(
                            now,
                            scheduledTask.getExecutionTime()
                    );

                    sleep(min(sleepDuration, Duration.ofMillis(100)));
                    continue;
                }

                if (!availableWorkerSlots.tryAcquire()) {
                    sleep(Duration.ofMillis(100));
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

        nextExecutionTime.ifPresent(nextTime -> {
            ScheduledTask nextScheduledTask = new ScheduledTask(
                    taskDefinition.getTaskId(),
                    nextTime
            );

            scheduledTaskQueue.offer(nextScheduledTask);
        });
    }

    private void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }

    private Duration min(Duration first, Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }
}
