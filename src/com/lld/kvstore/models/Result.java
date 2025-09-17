package com.lld.kvstore.models;

import java.time.Instant;
import java.util.Optional;

public class Result<T> {
    private final boolean success;
    private final T data;
    private final String error;
    private final Instant timestamp;
    
    private Result(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now();
    }
    
    public static <T> Result<T> ofSuccess(T data) {
        return new Result<>(true, data, null);
    }
    
    public static <T> Result<T> ofError(String error) {
        return new Result<>(false, null, error);
    }
    
    public static <T> Result<T> ofError(String error, Throwable throwable) {
        String fullError = error + ": " + throwable.getMessage();
        return new Result<>(false, null, fullError);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }
    
    public Optional<String> getError() {
        return Optional.ofNullable(error);
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public T getDataOrThrow() {
        if (isFailure()) {
            throw new IllegalStateException("Operation failed: " + error);
        }
        return data;
    }
    
    public T getDataOrDefault(T defaultValue) {
        return isSuccess() ? data : defaultValue;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "Result{success=true, data=" + data + ", timestamp=" + timestamp + "}";
        } else {
            return "Result{success=false, error='" + error + "', timestamp=" + timestamp + "}";
        }
    }
}
