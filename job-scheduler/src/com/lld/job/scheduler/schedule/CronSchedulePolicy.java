package com.lld.job.scheduler.schedule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

// Standard 5-field cron: minute hour day-of-month month day-of-week
// Supports *, lists (a,b), ranges (a-b), and steps (*/n, a-b/n, a/n).
// Day-of-week: 0 or 7 = Sunday, 1 = Monday, ..., 6 = Saturday.
// Vixie semantics: if both day-of-month and day-of-week are restricted, a day matches when EITHER matches.
public class CronSchedulePolicy implements SchedulePolicy {

    // Safety cap: a valid expression resolves within at most a few years; this bounds pathological inputs.
    private static final int MAX_ITERATIONS = 4 * 366 * 24;

    String cronExpression;
    ZoneId zoneId;

    NavigableSet<Integer> minutes;
    NavigableSet<Integer> hours;
    NavigableSet<Integer> daysOfMonth;
    NavigableSet<Integer> months;
    NavigableSet<Integer> daysOfWeek;
    boolean dayOfMonthRestricted;
    boolean dayOfWeekRestricted;

    public CronSchedulePolicy(String cronExpression) {
        this(cronExpression, ZoneId.of("UTC"));
    }

    public CronSchedulePolicy(String cronExpression, ZoneId zoneId) {
        this.cronExpression = cronExpression;
        this.zoneId = zoneId;

        String[] parts = cronExpression.trim().split("\\s+");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Cron expression must have 5 fields: " + cronExpression);
        }

        this.minutes = parseField(parts[0], 0, 59);
        this.hours = parseField(parts[1], 0, 23);
        this.daysOfMonth = parseField(normalizeWildcard(parts[2]), 1, 31);
        this.months = parseField(parts[3], 1, 12);
        this.daysOfWeek = parseDayOfWeek(normalizeWildcard(parts[4]));

        this.dayOfMonthRestricted = !isWildcard(parts[2]);
        this.dayOfWeekRestricted = !isWildcard(parts[4]);
    }

    @Override
    public Optional<Instant> nextExecutionAfter(Instant time) {
        ZonedDateTime candidate = time.atZone(zoneId)
                .plus(1, ChronoUnit.MINUTES)
                .truncatedTo(ChronoUnit.MINUTES);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (!months.contains(candidate.getMonthValue())) {
                candidate = candidate.plusMonths(1)
                        .withDayOfMonth(1)
                        .withHour(0)
                        .withMinute(0);
                continue;
            }
            if (!matchesDay(candidate)) {
                candidate = candidate.plusDays(1)
                        .withHour(0)
                        .withMinute(0);
                continue;
            }
            if (!hours.contains(candidate.getHour())) {
                Integer nextHour = hours.ceiling(candidate.getHour());
                if (nextHour == null) {
                    candidate = candidate.plusDays(1).withHour(0).withMinute(0);
                } else {
                    candidate = candidate.withHour(nextHour).withMinute(0);
                }
                continue;
            }
            if (!minutes.contains(candidate.getMinute())) {
                Integer nextMinute = minutes.ceiling(candidate.getMinute());
                if (nextMinute == null) {
                    candidate = candidate.plusHours(1).withMinute(0);
                } else {
                    candidate = candidate.withMinute(nextMinute);
                }
                continue;
            }
            return Optional.of(candidate.toInstant());
        }
        return Optional.empty();
    }

    private boolean matchesDay(ZonedDateTime dt) {
        int dom = dt.getDayOfMonth();
        // ISO: Mon=1..Sun=7  ->  cron: Mon=1..Sat=6, Sun=0
        int dow = dt.getDayOfWeek().getValue() % 7;

        boolean domMatch = daysOfMonth.contains(dom);
        boolean dowMatch = daysOfWeek.contains(dow);

        if (dayOfMonthRestricted && dayOfWeekRestricted) {
            return domMatch || dowMatch;
        }
        if (dayOfMonthRestricted) return domMatch;
        if (dayOfWeekRestricted) return dowMatch;
        return true;
    }

    private static boolean isWildcard(String field) {
        return field.equals("*") || field.equals("?");
    }

    private static String normalizeWildcard(String field) {
        return field.equals("?") ? "*" : field;
    }

    private static NavigableSet<Integer> parseField(String field, int min, int max) {
        NavigableSet<Integer> values = new TreeSet<>();
        for (String part : field.split(",")) {
            parsePart(part, min, max, values);
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cron field produced no values: " + field);
        }
        return values;
    }

    private static NavigableSet<Integer> parseDayOfWeek(String field) {
        NavigableSet<Integer> values = new TreeSet<>();
        for (String part : field.split(",")) {
            NavigableSet<Integer> partial = new TreeSet<>();
            parsePart(part, 0, 7, partial);
            // Collapse 7 (Sunday alias) to 0 so matching is consistent.
            for (int v : partial) {
                values.add(v == 7 ? 0 : v);
            }
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cron day-of-week produced no values: " + field);
        }
        return values;
    }

    private static void parsePart(String part, int min, int max, NavigableSet<Integer> out) {
        int step = 1;
        String range = part;
        boolean hasStep = part.contains("/");
        if (hasStep) {
            String[] sp = part.split("/");
            if (sp.length != 2) {
                throw new IllegalArgumentException("Invalid step in cron field: " + part);
            }
            range = sp[0];
            step = Integer.parseInt(sp[1]);
            if (step <= 0) {
                throw new IllegalArgumentException("Step must be positive: " + part);
            }
        }

        int rangeStart;
        int rangeEnd;
        if (range.equals("*")) {
            rangeStart = min;
            rangeEnd = max;
        } else if (range.contains("-")) {
            String[] rp = range.split("-");
            if (rp.length != 2) {
                throw new IllegalArgumentException("Invalid range in cron field: " + part);
            }
            rangeStart = Integer.parseInt(rp[0]);
            rangeEnd = Integer.parseInt(rp[1]);
        } else {
            int v = Integer.parseInt(range);
            rangeStart = v;
            // "n/step" means: starting at n, step until max.
            rangeEnd = hasStep ? max : v;
        }

        if (rangeStart < min || rangeEnd > max || rangeStart > rangeEnd) {
            throw new IllegalArgumentException(
                    "Range " + rangeStart + "-" + rangeEnd + " out of bounds [" + min + "," + max + "] in: " + part);
        }

        for (int i = rangeStart; i <= rangeEnd; i += step) {
            out.add(i);
        }
    }
}
