package com.lld.elevator.factory;


import com.lld.elevator.command.AsyncCommandBus;
import com.lld.elevator.command.CommandBus;

public class CommandBusFactory {
    public static CommandBus async(int capacity, int workers) {
        return new AsyncCommandBus(capacity, workers);
    }
}
