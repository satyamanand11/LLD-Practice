package com.lld.kvstore.api;

public class CommandBus {
    public Object dispatch(Command cmd) {
        return cmd.execute();
    }
}
