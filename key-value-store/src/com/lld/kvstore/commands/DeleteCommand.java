package com.lld.kvstore.commands;

import com.lld.kvstore.core.KeyValueStore;
import com.lld.kvstore.types.Result;

public class DeleteCommand implements Command {
    private final KeyValueStore store;
    private final String key;
    
    public DeleteCommand(KeyValueStore store, String key) {
        this.store = store;
        this.key = key;
    }
    
    @Override
    public Result<Void> execute() {
        return store.deleteKey(key);
    }
}
