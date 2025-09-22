package com.lld.kvstore.commands;

import com.lld.kvstore.core.KeyValueStore;
import com.lld.kvstore.types.Result;

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
    public Result<Void> execute() {
        return store.setPrimitive(key, value);
    }
}
