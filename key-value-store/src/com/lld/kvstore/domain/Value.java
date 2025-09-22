package com.lld.kvstore.domain;

public abstract class Value {
    public abstract TypeDescriptor type();
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract String toString();
}
