package com.lld.kvstore.storage;

import com.lld.kvstore.types.TypeDescriptor;

public class StorageEntry {
    private final String key;
    private final TypeDescriptor type;
    private final Object value;
    
    public StorageEntry(String key, TypeDescriptor type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public TypeDescriptor getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        StorageEntry that = (StorageEntry) obj;
        return java.util.Objects.equals(key, that.key) && 
               java.util.Objects.equals(type, that.type) && 
               java.util.Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, type, value);
    }
    
    @Override
    public String toString() {
        return "StorageEntry{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}
