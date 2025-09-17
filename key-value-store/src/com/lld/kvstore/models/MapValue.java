package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MapValue implements Value {
    private final Map<String, Object> values;
    private final ValueType type;
    private final ValueType valueType;
    private final ReadWriteLock lock;
    
    public MapValue(Map<String, Object> values, ValueType valueType) {
        this.values = new HashMap<>(values);
        this.type = ValueType.MAP;
        this.valueType = valueType;
        this.lock = new ReentrantReadWriteLock();
    }
    
    public MapValue(ValueType valueType) {
        this.values = new HashMap<>();
        this.type = ValueType.MAP;
        this.valueType = valueType;
        this.lock = new ReentrantReadWriteLock();
    }
    
    @Override
    public ValueType getType() {
        return type;
    }
    
    @Override
    public Object getValue() {
        lock.readLock().lock();
        try {
            return new HashMap<>(values);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public boolean isCollection() {
        return true;
    }
    
    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return values.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return values.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Value copy() {
        lock.readLock().lock();
        try {
            return new MapValue(values, valueType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Object putValue(String key, Object value) {
        validateValueType(value);
        
        lock.writeLock().lock();
        try {
            return values.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Object getValue(String key) {
        lock.readLock().lock();
        try {
            return values.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Object removeValue(String key) {
        lock.writeLock().lock();
        try {
            return values.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean containsKey(String key) {
        lock.readLock().lock();
        try {
            return values.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean containsValue(Object value) {
        lock.readLock().lock();
        try {
            return values.containsValue(value);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Set<String> getKeys() {
        lock.readLock().lock();
        try {
            return new HashSet<>(values.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Collection<Object> getValues() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(values.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Set<Map.Entry<String, Object>> getEntries() {
        lock.readLock().lock();
        try {
            return new HashSet<>(values.entrySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void clearValues() {
        lock.writeLock().lock();
        try {
            values.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public ValueType getValueType() {
        return valueType;
    }
    
    protected void validateValueType(Object value) {
        if (value == null) {
            return;
        }
        
        ValueType actualType = ValueType.fromObject(value);
        if (actualType != valueType && !isCompatibleType(actualType, valueType)) {
            throw new IllegalArgumentException(
                "Value type " + actualType + " is not compatible with map value type " + valueType
            );
        }
    }
    
    protected boolean isCompatibleType(ValueType actual, ValueType expected) {
        if (actual.isPrimitive() && expected.isPrimitive()) {
            return (actual == ValueType.INTEGER || actual == ValueType.LONG || 
                   actual == ValueType.DOUBLE || actual == ValueType.FLOAT) &&
                   (expected == ValueType.INTEGER || expected == ValueType.LONG || 
                   expected == ValueType.DOUBLE || expected == ValueType.FLOAT);
        }
        return actual == expected;
    }
    
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return values.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MapValue that = (MapValue) obj;
        
        lock.readLock().lock();
        try {
            return type == that.type && 
                   valueType == that.valueType && 
                   Objects.equals(values, that.values);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return Objects.hash(values, type, valueType);
        } finally {
            lock.readLock().unlock();
        }
    }
}