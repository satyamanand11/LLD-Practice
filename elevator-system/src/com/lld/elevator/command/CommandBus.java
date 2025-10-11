package com.lld.elevator.command;

public interface CommandBus {
    void submit(Command c);
    void shutdown();
}
