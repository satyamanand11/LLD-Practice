package com.lld.kvstore.commands;

import com.lld.kvstore.core.KeyValueStore;

public class GetCommand implements Command {
    private final KeyValueStore store;
    private final String key;
    
    public GetCommand(KeyValueStore store, String key) {
        this.store = store;
        this.key = key;
    }
    
    @Override
    public Object execute() {
        return store.get(key);
    }
}
