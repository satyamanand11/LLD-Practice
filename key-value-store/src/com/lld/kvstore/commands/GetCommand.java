package com.lld.kvstore.commands;

import com.lld.kvstore.enums.CommandType;
import com.lld.kvstore.interfaces.Command;
import com.lld.kvstore.interfaces.KeyValueStore;
import com.lld.kvstore.interfaces.Value;
import com.lld.kvstore.models.Result;

public class GetCommand implements Command<Value> {
    private final String key;
    
    public GetCommand(String key) {
        this.key = key;
    }
    
    @Override
    public Result<Value> execute(KeyValueStore store) {
        return store.get(key);
    }
    
    @Override
    public CommandType getCommandType() {
        return CommandType.GET;
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
        return "Get value for key: " + key;
    }
}
