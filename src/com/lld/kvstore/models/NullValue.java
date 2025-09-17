package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;
import com.lld.kvstore.interfaces.Value;

public class NullValue implements Value {
    private static final NullValue INSTANCE = new NullValue();
    
    private NullValue() {
    }
    
    public static NullValue getInstance() {
        return INSTANCE;
    }
    
    @Override
    public ValueType getType() {
        return ValueType.NULL;
    }
    
    @Override
    public Object getValue() {
        return null;
    }
    
    @Override
    public boolean isNull() {
        return true;
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
    
    @Override
    public boolean isCollection() {
        return false;
    }
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public Value copy() {
        return this;
    }
    
    @Override
    public String toString() {
        return "null";
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullValue;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
}