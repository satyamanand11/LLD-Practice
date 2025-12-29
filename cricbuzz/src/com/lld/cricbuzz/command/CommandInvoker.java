package com.lld.cricbuzz.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Command Invoker - Executes and manages commands
 * Supports command queuing, history, and undo/redo
 * Thread-safe implementation
 */
public class CommandInvoker {
    private final Queue<Command> commandQueue;
    private final List<Command> commandHistory;
    private final int maxHistorySize;
    private int undoPointer; // Points to current position in history

    public CommandInvoker() {
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.commandHistory = new ArrayList<>();
        this.maxHistorySize = 100; // Keep last 100 commands
        this.undoPointer = -1;
    }

    /**
     * Execute a command immediately
     */
    public CommandResult execute(Command command) {
        if (command == null) {
            return CommandResult.failure("Command cannot be null");
        }

        CommandResult result = command.execute();
        
        if (result.isSuccess()) {
            // Add to history (remove any commands after undo pointer)
            if (undoPointer < commandHistory.size() - 1) {
                commandHistory.subList(undoPointer + 1, commandHistory.size()).clear();
            }
            
            commandHistory.add(command);
            undoPointer = commandHistory.size() - 1;
            
            // Limit history size
            if (commandHistory.size() > maxHistorySize) {
                commandHistory.remove(0);
                undoPointer--;
            }
        }
        
        return result;
    }

    /**
     * Queue a command for later execution
     */
    public void queue(Command command) {
        if (command != null) {
            commandQueue.offer(command);
        }
    }

    /**
     * Process all queued commands
     */
    public List<CommandResult> processQueue() {
        List<CommandResult> results = new ArrayList<>();
        while (!commandQueue.isEmpty()) {
            Command command = commandQueue.poll();
            if (command != null) {
                results.add(execute(command));
            }
        }
        return results;
    }

    /**
     * Undo the last executed command
     */
    public CommandResult undo() {
        if (undoPointer < 0 || undoPointer >= commandHistory.size()) {
            return CommandResult.failure("No command to undo");
        }

        Command command = commandHistory.get(undoPointer);
        if (!command.canUndo()) {
            return CommandResult.failure("Command cannot be undone: " + command.getDescription());
        }

        CommandResult result = command.undo();
        if (result.isSuccess()) {
            undoPointer--;
        }
        
        return result;
    }

    /**
     * Redo the last undone command
     */
    public CommandResult redo() {
        if (undoPointer >= commandHistory.size() - 1) {
            return CommandResult.failure("No command to redo");
        }

        undoPointer++;
        Command command = commandHistory.get(undoPointer);
        return command.execute();
    }

    /**
     * Get command history
     */
    public List<Command> getHistory() {
        return new ArrayList<>(commandHistory);
    }

    /**
     * Get commands executed by a specific user
     */
    public List<Command> getHistoryByExecutor(String executorId) {
        return commandHistory.stream()
            .filter(cmd -> cmd.getExecutorId().equals(executorId))
            .toList();
    }

    /**
     * Clear command history
     */
    public void clearHistory() {
        commandHistory.clear();
        undoPointer = -1;
    }

    /**
     * Check if undo is available
     */
    public boolean canUndo() {
        return undoPointer >= 0 && undoPointer < commandHistory.size() &&
               commandHistory.get(undoPointer).canUndo();
    }

    /**
     * Check if redo is available
     */
    public boolean canRedo() {
        return undoPointer < commandHistory.size() - 1;
    }
}

