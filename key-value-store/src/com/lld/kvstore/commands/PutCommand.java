package com.lld.kvstore.commands;

import com.lld.kvstore.enums.CommandType;
import com.lld.kvstore.interfaces.Command;
import com.lld.kvstore.interfaces.KeyValueStore;
import com.lld.kvstore.models.Result;

public class PutCommand implements Command<Boolean> {
    private final String key;
    private final Object value;
    
    public PutCommand(String key, Object value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public Result<Boolean> execute(KeyValueStore store) {
        return store.put(key, value);
    }
    
    @Override
    public CommandType getCommandType() {
        return CommandType.PUT;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public boolean isValid() {
        return key != null && value != null;
    }
    
    @Override
    public String getDescription() {
        return "Put key-value pair: " + key + " -> " + value;
    }
}
