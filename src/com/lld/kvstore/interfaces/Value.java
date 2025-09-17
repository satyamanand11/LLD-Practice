package com.lld.kvstore.interfaces;

import com.lld.kvstore.enums.ValueType;

public interface Value {
    ValueType getType();
    Object getValue();
    boolean isNull();
    boolean isPrimitive();
    boolean isCollection();
    int size();
    Value copy();
    String toString();
    boolean equals(Object obj);
    int hashCode();
}
