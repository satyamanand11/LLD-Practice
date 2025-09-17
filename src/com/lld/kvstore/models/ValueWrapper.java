package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;
import com.lld.kvstore.interfaces.Value;

import java.time.Instant;
import java.util.Objects;

public class ValueWrapper {
    private volatile Value value;
    private final ValueType type;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    
    public ValueWrapper(Value value) {
        this.value = value;
        this.type = value.getType();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public Value getValue() {
        return value;
    }
    
    public ValueType getType() {
        return type;
    }
    
    public synchronized void updateValue(Value newValue) {
        if (newValue.getType() != type) {
            throw new IllegalArgumentException(
                "Cannot update value with different type. Expected: " + type + 
                ", Got: " + newValue.getType()
            );
        }
        this.value = newValue;
        this.updatedAt = Instant.now();
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isNull() {
        return value.isNull();
    }
    
    public int size() {
        return value.size();
    }
    
    public boolean isEmpty() {
        return value.isEmpty();
    }
    
    public ValueWrapper copy() {
        return new ValueWrapper(value.copy());
    }
    
    @Override
    public String toString() {
        return "ValueWrapper{" +
                "value=" + value +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ValueWrapper that = (ValueWrapper) obj;
        return Objects.equals(value, that.value) && 
               type == that.type &&
               Objects.equals(createdAt, that.createdAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, type, createdAt);
    }
}
