package com.lld.kvstore.commands;

public class CommandBus {
    public Object dispatch(Command command) {
        return command.execute();
    }
}
