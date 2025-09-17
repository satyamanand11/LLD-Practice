package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;

import java.util.*;

public class SetValue extends CollectionValue {
    
    public SetValue(Set<Object> values, ValueType elementType) {
        super(new HashSet<>(values), ValueType.SET, elementType);
    }
    
    public SetValue(ValueType elementType) {
        super(new HashSet<>(), ValueType.SET, elementType);
    }
    
    public boolean addAllValues(Collection<Object> values) {
        boolean added = false;
        for (Object value : values) {
            validateElementType(value);
            if (addValue(value)) {
                added = true;
            }
        }
        return added;
    }
    
    public boolean removeAllValues(Collection<Object> values) {
        lock.writeLock().lock();
        try {
            return this.values.removeAll(values);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean retainAllValues(Collection<Object> values) {
        lock.writeLock().lock();
        try {
            return this.values.retainAll(values);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean containsAllValues(Collection<Object> values) {
        lock.readLock().lock();
        try {
            return this.values.containsAll(values);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    protected CollectionValue createCopy() {
        lock.readLock().lock();
        try {
            return new SetValue((Set<Object>) values, elementType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    protected Collection<Object> getCollectionCopy() {
        return new HashSet<>((Set<Object>) values);
    }
    
    @SuppressWarnings("unchecked")
    protected Set<Object> getSet() {
        return (Set<Object>) values;
    }
}