package com.lld.cricbuzz.command;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of command execution
 */
public class CommandResult {
    private final boolean success;
    private final String message;
    private final Object result;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;

    public CommandResult(boolean success, String message) {
        this(success, message, null);
    }

    public CommandResult(boolean success, String message, Object result) {
        this.success = success;
        this.message = message;
        this.result = result;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getResult() {
        return result;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    public static CommandResult success(String message) {
        return new CommandResult(true, message);
    }

    public static CommandResult success(String message, Object result) {
        return new CommandResult(true, message, result);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message);
    }
}

