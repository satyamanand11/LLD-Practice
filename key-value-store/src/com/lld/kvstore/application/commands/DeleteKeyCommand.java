package com.lld.kvstore.application.commands;

import com.lld.kvstore.api.Command;
import com.lld.kvstore.api.KeyValueStore;

public class DeleteKeyCommand implements Command {
    private final KeyValueStore store;
    private final String key;
    
    public DeleteKeyCommand(KeyValueStore store, String key) {
        this.store = store;
        this.key = key;
    }
    
    @Override
    public Object execute() {
        return store.deleteKey(key);
    }
}
