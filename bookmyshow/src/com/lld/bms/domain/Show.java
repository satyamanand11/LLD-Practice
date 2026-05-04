package com.lld.bms.domain;

import java.time.LocalDateTime;

public class Show {
    private final String id;
    private final String movieId;
    private final String screenId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private ShowStatus status;
    private final LocalDateTime createdAt;

    public Show(String id, String movieId, String screenId,
                LocalDateTime startTime, LocalDateTime endTime,
                ShowStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getMovieId() { return movieId; }
    public String getScreenId() { return screenId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public ShowStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void markOngoing() { this.status = ShowStatus.ONGOING; }
    public void markCompleted() { this.status = ShowStatus.COMPLETED; }
    public void cancel() { this.status = ShowStatus.CANCELLED; }
}
