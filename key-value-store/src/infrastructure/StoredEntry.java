package com.lld.kvstore.infrastructure;

import com.lld.kvstore.domain.TypeDescriptor;

public class StoredEntry {
    private final String key;
    private final TypeDescriptor type;
    private final Object payload;
    
    public StoredEntry(String key, TypeDescriptor type, Object payload) {
        this.key = key;
        this.type = type;
        this.payload = payload;
    }
    
    public String getKey() {
        return key;
    }
    
    public TypeDescriptor getType() {
        return type;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        StoredEntry that = (StoredEntry) obj;
        return java.util.Objects.equals(key, that.key) && 
               java.util.Objects.equals(type, that.type) && 
               java.util.Objects.equals(payload, that.payload);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(key, type, payload);
    }
    
    @Override
    public String toString() {
        return "StoredEntry{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", payload=" + payload +
                '}';
    }
}
