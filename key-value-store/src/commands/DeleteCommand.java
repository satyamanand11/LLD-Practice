package com.lld.kvstore.commands;

import com.lld.kvstore.core.KeyValueStore;

public class DeleteCommand implements Command {
    private final KeyValueStore store;
    private final String key;
    
    public DeleteCommand(KeyValueStore store, String key) {
        this.store = store;
        this.key = key;
    }
    
    @Override
    public Object execute() {
        return store.deleteKey(key);
    }
}
