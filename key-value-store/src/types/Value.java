package com.lld.kvstore.types;

public abstract class Value {
    public abstract TypeDescriptor getType();
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract String toString();
}
