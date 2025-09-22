package com.lld.kvstore.com.lld.kvstore.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ListValue extends Value {
    private final List<Object> values;
    private final TypeDescriptor typeDescriptor;
    private final ReadWriteLock lock;
    
    public ListValue(List<Object> values, com.lld.kvstore.domain.PrimitiveKind primitiveKind) {
        this.values = new ArrayList<>(values);
        this.typeDescriptor = new TypeDescriptor(HolderType.LIST, primitiveKind);
        this.lock = new ReentrantReadWriteLock();
    }
    
    public List<Object> get() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(values);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean add(Object value) {
        lock.writeLock().lock();
        try {
            return values.add(value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean remove(Object value) {
        lock.writeLock().lock();
        try {
            return values.remove(value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int size() {
        lock.readLock().lock();
        try {
            return values.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public TypeDescriptor type() {
        return typeDescriptor;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ListValue that = (ListValue) obj;
        lock.readLock().lock();
        try {
            return java.util.Objects.equals(values, that.values) && 
                   java.util.Objects.equals(typeDescriptor, that.typeDescriptor);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return java.util.Objects.hash(values, typeDescriptor);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return "ListValue{" +
                    "values=" + values +
                    ", type=" + typeDescriptor +
                    '}';
        } finally {
            lock.readLock().unlock();
        }
    }
}
