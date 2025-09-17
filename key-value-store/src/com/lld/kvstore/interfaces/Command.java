package com.lld.kvstore.interfaces;

import com.lld.kvstore.enums.CommandType;
import com.lld.kvstore.models.Result;

public interface Command<T> {
    Result<T> execute(KeyValueStore store);
    CommandType getCommandType();
    String getKey();
    boolean isValid();
    String getDescription();
}
