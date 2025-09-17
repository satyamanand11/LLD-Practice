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
        if (!isValid()) {
            return Result.ofError("Invalid command: key cannot be null or empty");
        }
        
        try {
            return store.put(key, value);
        } catch (Exception e) {
            return Result.ofError("Failed to execute PUT command", e);
        }
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
        return key != null && !key.trim().isEmpty();
    }
    
    @Override
    public String getDescription() {
        return "Store value '" + value + "' against key '" + key + "'";
    }
    
    public Object getValue() {
        return value;
    }
}
