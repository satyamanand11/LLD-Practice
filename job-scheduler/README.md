# Job Scheduler — LLD

An in-memory, multi-threaded job scheduler that supports one-time, fixed-interval and cron-based schedules. Tasks can be scheduled, paused, resumed, and cancelled; executions are persisted (in-memory) for inspection.

## 1. Requirements

### 1.1 Functional

1. Users can register tasks to be scheduled.
2. The scheduled time must be in the future.
3. At the scheduled time the task should be executed.
4. Schedules can be one of:
   - **One-time** — fires once at a specific instant.
   - **Fixed-interval** — fires at `startTime`, `startTime + interval`, `startTime + 2*interval`, …
   - **Cron** — standard 5-field cron (`min hour day-of-month month day-of-week`).
5. Tasks execute concurrently on a worker pool of bounded size (e.g. 10 threads).
6. When more tasks are due than there are workers (e.g. 100 tasks at 22:00 with 10 workers), a configurable **priority policy** decides who runs first (earliest-due-first by default).
7. Users can **cancel** a scheduled task. They can also **pause** / **resume** it.
8. Execution history (status, start/finish time, error message) should be queryable per task.

### 1.2 Non-functional

- **Thread-safe** — all shared state is guarded (concurrent collections, semaphore, volatile flags).
- **Bounded resources** — fixed-size worker pool; no unbounded queue growth from a single task burst.
- **Extensible** — new schedule types or priority policies plug in without touching the dispatcher.
- **Graceful shutdown** — dispatcher stops, in-flight tasks finish, no orphan threads.

### 1.3 Out of scope (explicit non-goals)

- Persistence across JVM restarts (everything is in-memory).
- Distributed scheduling / leader election (single-node).
- Retries with backoff (single attempt; can be added by wrapping `TaskHandler`).
- Misfire policies (run-once-after-recovery, skip, etc.).
- Time-zone-aware schedules per task (cron supports zone but the rest run on UTC `Instant`).

## 2. Public API (Facade)

Clients only ever talk to `JobSchedulerSystem`.

```java
JobSchedulerSystem scheduler = new JobSchedulerSystemImpl(
        new SchedulerConfig(10, TaskPriorityPolicy.EARLIEST_EXECUTION_TIME_FIRST));

scheduler.start();

String taskId = scheduler.scheduleTask(
        "nightly-report",
        new CronSchedulePolicy("0 2 * * *"),
        ctx -> reportService.run());

scheduler.pauseTask(taskId);
scheduler.resumeTask(taskId);
scheduler.cancelTask(taskId);

List<TaskExecution> history = scheduler.getExecutions(taskId);

scheduler.stop();
```

## 3. Component overview

```
                         ┌──────────────────────────┐
                         │    JobSchedulerSystem    │  facade interface
                         └────────────┬─────────────┘
                                      │
                         ┌────────────▼─────────────┐
                         │  JobSchedulerSystemImpl  │  wires everything
                         └────┬─────────────┬───────┘
                              │             │
            ┌─────────────────▼──┐     ┌────▼─────────────────────┐
            │ TaskSchedulerService│◄───►│ TaskDefinitionRepository │
            │  (dispatcher loop) │     └──────────────────────────┘
            └─────┬───────────┬──┘
                  │           │
       ┌──────────▼┐   ┌──────▼─────────────────┐
       │ Worker    │   │ TaskExecutionService    │
       │ pool      │──►│ + TaskExecutionRepo    │
       └───────────┘   └────────────────────────┘
```

### 3.1 Layers

| Layer        | Type(s)                                                     | Responsibility |
|--------------|-------------------------------------------------------------|----------------|
| Facade       | `JobSchedulerSystem`, `JobSchedulerSystemImpl`              | Public API, wires services. Generates `taskId`, validates input. |
| Service      | `TaskSchedulerService`, `TaskExecutionService`              | Dispatcher loop, worker pool, individual execution lifecycle. |
| Domain model | `TaskDefinition`, `ScheduledTask`, `TaskExecution`, `TaskExecutionContext`, enums | Business entities. |
| Schedule     | `SchedulePolicy` + `OneTimeSchedulePolicy` / `FixedIntervalSchedulePolicy` / `CronSchedulePolicy` | Strategy: "when does this fire next?" |
| Handler      | `TaskHandler` (interface), `EmailReportTaskHandler` (sample) | Strategy: "what does the task do?" |
| Repository   | `TaskDefinitionRepository`, `TaskExecutionRepository`       | In-memory `ConcurrentHashMap` storage. |
| Config       | `SchedulerConfig`, `TaskPriorityPolicy`                     | Runtime configuration. |

### 3.2 Dispatcher loop (heart of the system)

`TaskSchedulerService` holds a `PriorityBlockingQueue<ScheduledTask>` ordered by execution time (per the configured `TaskPriorityPolicy`). A single dispatcher thread runs:

1. Peek the head.
2. If empty → short sleep.
3. If head's `executionTime` is in the future → sleep `min(timeUntilHead, 100ms)`.
4. Else acquire a worker permit from a `Semaphore` sized to the pool. If none free → short sleep.
5. Poll the head and submit it to the `ExecutorService` worker pool.
6. The worker runs `TaskExecutionService.execute(...)`, then re-arms the next fire-time, then releases the permit.

The `Semaphore` plus `PriorityBlockingQueue` is what gives us **work conservation under back-pressure**: when 100 tasks become due and we only have 10 workers, the highest-priority 10 run, the dispatcher waits, and the remaining 90 are picked up in priority order as workers free up.

### 3.3 Priority policy

Pluggable comparator built by `ScheduledTaskComparatorFactory`:

- `EARLIEST_EXECUTION_TIME_FIRST` (FIFO by due-time) — the sane default.
- `LATEST_EXECUTION_TIME_FIRST` (LIFO) — provided for symmetry / interview talking point.

Adding a new policy is a 2-line change (enum constant + comparator).

### 3.4 Schedule strategy

`SchedulePolicy` has a single method: `Optional<Instant> nextExecutionAfter(Instant t)`.

Returning `Optional.empty()` means "no further executions"; the scheduler then marks the task `COMPLETED` (used by `OneTimeSchedulePolicy` after it has fired and by `CronSchedulePolicy` for impossible expressions).

This single method is what lets the dispatcher treat all three schedule kinds uniformly.

## 4. Concurrency model

- **`PriorityBlockingQueue`** — thread-safe priority queue; producers (`schedule`, reschedule, resume) and the consumer (dispatcher) are decoupled.
- **`Semaphore` + fixed `ExecutorService`** — bounds the number of in-flight tasks. The semaphore is the *gate*; the executor is the *engine*. We never over-submit.
- **`ConcurrentHashMap`** in repos — safe `put` / `get` / `remove` from any thread.
- **`volatile boolean running`** + interrupt — clean cooperative shutdown.
- **`volatile TaskStatus`** on `TaskDefinition` — paused/cancelled is observed by both the dispatcher and the worker.
- **`synchronized start()/stop()`** — idempotent lifecycle, prevents two dispatcher threads.

### 4.1 Cancellation semantics

`cancel(taskId)`:
1. Flips status → `CANCELLED`.
2. Eagerly removes any pending `ScheduledTask` for that id from the queue.
3. Any in-flight worker for that id is allowed to finish; the next reschedule is skipped because `isActive()` returns `false`.

This avoids the messy "interrupt the worker" path and keeps cancellation correct even if the queue removal races with the dispatcher (the worker will still re-check `isActive()` before running and again before rescheduling).

### 4.2 Pause/resume semantics

- `pause` — flips status → `PAUSED`. Pending entries stay in the queue but the worker skips them (`SKIPPED` execution recorded). No further reschedules.
- `resume` — flips status → `ACTIVE` and re-arms the next fire-time if no entry is currently queued for that id.

## 5. Cron parser

Standard 5-field cron: `minute hour day-of-month month day-of-week`.

- Supports `*`, `?` (alias of `*`), lists `a,b`, ranges `a-b`, steps `*/n`, `a-b/n`, `a/n`.
- Day-of-week: `0` or `7` = Sunday, `1` = Monday, …, `6` = Saturday.
- **Vixie semantics**: when both day-of-month and day-of-week are restricted, a day matches if **either** matches. When only one is restricted, only that one is used.
- Time zone defaults to UTC; can be passed explicitly.
- Returns `Optional.empty()` for impossible expressions (e.g. `0 0 31 2 *` — Feb 31).

### 5.1 Verification

A standalone test harness (run during development) covers:

| # | Expression | From | Expected |
|---|------------|------|----------|
| 1 | `* * * * *` | 10:00:30 | 10:01:00 |
| 2 | `0 * * * *` | 10:00:30 | 11:00:00 |
| 3 | `0 0 * * *` | 10:00 | next 00:00 |
| 4 | `0 9 * * 1-5` | Wed 10:00 | Thu 09:00 |
| 5 | `0 9 * * 1-5` | Sat | Mon 09:00 |
| 6 | `*/15 * * * *` | 10:07 | 10:15 |
| 7 | `30 14 1 * *` | mid-month | next 1st 14:30 |
| 8 | `0 12 * * 0` | Wed | next Sun 12:00 |
| 9 | `0 12 * * 7` | Wed | next Sun 12:00 |
| 10 | `0 0 15 * 1` (Vixie) | early week | next Mon |
| 11 | `0 0 15 * 1` (Vixie) | after Mon | the 15th |
| 12 | `0-30/10 * * * *` | 10:25 | 10:30 |
| 13 | `0-30/10 * * * *` | 10:35 | 11:00 |
| 14 | `0 9-17 * * *` | 18:00 | next 09:00 |
| 15 | `0 0 29 2 *` | 2026 | 2028-02-29 (leap) |
| 16 | `0 0 31 2 *` | any | empty (impossible) |
| 17 | `5,10,15 * * * *` | 10:11 | 10:15 |
| 18 | `0 0 1 1 *` | Jun | next Jan 1 |
| 19 | `0 0 1 * ?` | any | next 1st 00:00 |

All 19 pass.

### 5.2 Known limitations

- No English aliases (`MON`, `JAN`) and no special expressions (`@daily`, `@hourly`).
- No 6th seconds field (Quartz-style); minute granularity only.
- DST: behaviour around spring-forward/fall-back uses `ZonedDateTime`'s default resolution, which may skip or repeat a fire on the boundary minute. Run in UTC to avoid this.

## 6. Design patterns used (for the interview)

- **Facade** — `JobSchedulerSystem` hides services, repositories and the worker pool.
- **Strategy** — `SchedulePolicy` (when to run) and `TaskPriorityPolicy` (which-due-task to run first).
- **Factory** — `ScheduledTaskComparatorFactory` builds the comparator from the policy enum.
- **Repository** — `TaskDefinitionRepository`, `TaskExecutionRepository` abstract storage.
- **Producer-consumer** — `schedule()` / reschedule produce; the dispatcher consumes.
- **Template/Command-ish** — `TaskHandler` is the unit of work the executor invokes.

## 7. SOLID review

- **S**: each class has one reason to change — `TaskSchedulerService` orchestrates timing, `TaskExecutionService` runs one task, repos store, schedules compute fire-times.
- **O**: new schedule kinds and priority policies plug in without modifying the dispatcher.
- **L**: every `SchedulePolicy` is interchangeable; nothing in the dispatcher knows the concrete type.
- **I**: `SchedulePolicy` and `TaskHandler` are minimal single-method interfaces.
- **D**: `TaskSchedulerService` depends on `SchedulePolicy` and `TaskDefinitionRepository` abstractions, not concrete classes; the facade does the wiring.

## 8. Possible extensions (good talking points)

| Extension | Sketch |
|-----------|--------|
| Persistence | Replace `ConcurrentHashMap` repos with a JDBC/SQL implementation; recover queue from DB on `start()`. |
| Retries | Wrap `TaskHandler.execute()` in a `RetryingTaskHandler` decorator with an exponential-backoff `RetryPolicy`. |
| Misfire policy | On `start()`, for any task whose last expected fire-time is in the past, decide between *fire-now*, *skip-to-next* or *skip-all-misses*. |
| Distributed mode | Replace the in-memory queue with a leased work queue (e.g. SQS, Redis, Postgres `SELECT ... FOR UPDATE SKIP LOCKED`); workers across nodes lease then ack. |
| Per-task concurrency limit | Add a `ConcurrentHashMap<taskId, Semaphore>` so the same task never runs in parallel with itself. |
| Observability | Emit metrics on dispatch latency, queue depth, success/failure counts. |
| Time-zone-aware non-cron schedules | Make `OneTimeSchedulePolicy` accept `ZonedDateTime` for human-friendly scheduling. |

## 9. Running the demo

The included `Main` exercises every feature: one-time, fixed-interval, cron, a 12-task burst against a 4-worker pool, plus cancel/pause/resume.

```bash
javac -d out $(find src -name "*.java")
java -cp out Main
```

Sample output:

```
[main] Job scheduler started
[heartbeat] tick at 2026-05-06T15:10:38Z
[one-time] hello at 2026-05-06T15:10:39Z
[burst-2] ran at 2026-05-06T15:10:41Z
[burst-0] ran at 2026-05-06T15:10:41Z
... (waves of 4 due to 4-worker pool) ...
[main] Cancelled heartbeat task? true
[main] Paused cron task
[main] Resumed cron task
[main] One-time executions:
  - id=ba5e55ca-... status=SUCCESS started=... completed=...
[main] Job scheduler stopped
```
