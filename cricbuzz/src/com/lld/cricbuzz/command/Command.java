package com.lld.cricbuzz.command;

import java.time.LocalDateTime;

/**
 * Command interface for Command Pattern
 * Encapsulates user-triggered operations as objects
 * Supports execute, undo, and logging
 */
public interface Command {
    /**
     * Execute the command
     * @return CommandResult containing execution status and result
     */
    CommandResult execute();
    
    /**
     * Undo the command (if supported)
     * @return CommandResult containing undo status
     */
    CommandResult undo();
    
    /**
     * Check if command can be undone
     */
    boolean canUndo();
    
    /**
     * Get command description for logging
     */
    String getDescription();
    
    /**
     * Get timestamp when command was created
     */
    LocalDateTime getTimestamp();
    
    /**
     * Get the user/admin who triggered this command
     */
    String getExecutorId();
}

