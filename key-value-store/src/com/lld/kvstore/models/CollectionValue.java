package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;
import com.lld.kvstore.interfaces.Value;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class CollectionValue implements Value {
    protected final Collection<Object> values;
    protected final ValueType type;
    protected final ValueType elementType;
    protected final ReadWriteLock lock;
    
    protected CollectionValue(Collection<Object> values, ValueType type, ValueType elementType) {
        this.values = new ArrayList<>(values);
        this.type = type;
        this.elementType = elementType;
        this.lock = new ReentrantReadWriteLock();
        
        if (!type.isCollection()) {
            throw new IllegalArgumentException("Type must be a collection type: " + type);
        }
    }
    
    @Override
    public ValueType getType() {
        return type;
    }
    
    @Override
    public Object getValue() {
        lock.readLock().lock();
        try {
            return getCollectionCopy();
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
            return createCopy();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean addValue(Object value) {
        validateElementType(value);
        
        lock.writeLock().lock();
        try {
            return values.add(value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean removeValue(Object value) {
        lock.writeLock().lock();
        try {
            return values.remove(value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean containsValue(Object value) {
        lock.readLock().lock();
        try {
            return values.contains(value);
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
    
    public Collection<Object> getValues() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableCollection(new ArrayList<>(values));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public ValueType getElementType() {
        return elementType;
    }
    
    protected void validateElementType(Object value) {
        if (value == null) {
            return;
        }
        
        ValueType actualType = ValueType.fromObject(value);
        if (actualType != elementType && !isCompatibleType(actualType, elementType)) {
            throw new IllegalArgumentException(
                "Value type " + actualType + " is not compatible with collection element type " + elementType
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
    
    protected abstract CollectionValue createCopy();
    protected abstract Collection<Object> getCollectionCopy();
    
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
        
        CollectionValue that = (CollectionValue) obj;
        
        lock.readLock().lock();
        try {
            return type == that.type && 
                   elementType == that.elementType && 
                   Objects.equals(values, that.values);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return Objects.hash(values, type, elementType);
        } finally {
            lock.readLock().unlock();
        }
    }
}