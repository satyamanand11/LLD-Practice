package com.lld.kvstore.application.commands;

import com.lld.kvstore.api.Command;
import com.lld.kvstore.api.KeyValueStore;
import com.lld.kvstore.api.Result;

public class SetPrimitiveCommand implements Command {
    private final KeyValueStore store;
    private final String key;
    private final Object value;
    
    public SetPrimitiveCommand(KeyValueStore store, String key, Object value) {
        this.store = store;
        this.key = key;
        this.value = value;
    }
    
    @Override
    public Object execute() {
        return store.setPrimitive(key, value);
    }
}
