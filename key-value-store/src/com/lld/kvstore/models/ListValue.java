package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;

import java.util.*;

public class ListValue extends CollectionValue {
    
    public ListValue(List<Object> values, ValueType elementType) {
        super(new ArrayList<>(values), ValueType.LIST, elementType);
    }
    
    public ListValue(ValueType elementType) {
        super(new ArrayList<>(), ValueType.LIST, elementType);
    }
    
    public Object getValue(int index) {
        lock.readLock().lock();
        try {
            if (index < 0 || index >= values.size()) {
                return null;
            }
            return ((List<Object>) values).get(index);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Object setValue(int index, Object value) {
        validateElementType(value);
        
        lock.writeLock().lock();
        try {
            if (index < 0 || index >= values.size()) {
                return null;
            }
            return ((List<Object>) values).set(index, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean addValue(int index, Object value) {
        validateElementType(value);
        
        lock.writeLock().lock();
        try {
            if (index < 0 || index > values.size()) {
                return false;
            }
            ((List<Object>) values).add(index, value);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Object removeValue(int index) {
        lock.writeLock().lock();
        try {
            if (index < 0 || index >= values.size()) {
                return null;
            }
            return ((List<Object>) values).remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int indexOf(Object value) {
        lock.readLock().lock();
        try {
            return ((List<Object>) values).indexOf(value);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int lastIndexOf(Object value) {
        lock.readLock().lock();
        try {
            return ((List<Object>) values).lastIndexOf(value);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    protected CollectionValue createCopy() {
        lock.readLock().lock();
        try {
            return new ListValue((List<Object>) values, elementType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    protected Collection<Object> getCollectionCopy() {
        return new ArrayList<>((List<Object>) values);
    }
    
    @SuppressWarnings("unchecked")
    protected List<Object> getList() {
        return (List<Object>) values;
    }
}