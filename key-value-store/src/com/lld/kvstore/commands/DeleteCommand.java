package com.lld.kvstore.commands;

import com.lld.kvstore.enums.CommandType;
import com.lld.kvstore.interfaces.Command;
import com.lld.kvstore.interfaces.KeyValueStore;
import com.lld.kvstore.models.Result;

public class DeleteCommand implements Command<Boolean> {
    private final String key;
    
    public DeleteCommand(String key) {
        this.key = key;
    }
    
    @Override
    public Result<Boolean> execute(KeyValueStore store) {
        return store.delete(key);
    }
    
    @Override
    public CommandType getCommandType() {
        return CommandType.DELETE;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public boolean isValid() {
        return key != null;
    }
    
    @Override
    public String getDescription() {
        return "Delete key: " + key;
    }
}
