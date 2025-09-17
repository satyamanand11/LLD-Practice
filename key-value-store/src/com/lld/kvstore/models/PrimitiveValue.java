package com.lld.kvstore.models;

import com.lld.kvstore.enums.ValueType;
import com.lld.kvstore.interfaces.Value;

import java.util.Objects;


public class PrimitiveValue implements Value {
    private final Object value;
    private final ValueType type;
    
    public PrimitiveValue(Object value) {
        this.value = value;
        this.type = ValueType.fromObject(value);
        
        if (!type.isPrimitive() && type != ValueType.NULL) {
            throw new IllegalArgumentException("Value must be primitive: " + value.getClass());
        }
    }
    
    @Override
    public ValueType getType() {
        return type;
    }
    
    @Override
    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean isNull() {
        return type == ValueType.NULL || value == null;
    }
    
    @Override
    public boolean isPrimitive() {
        return true;
    }
    
    @Override
    public boolean isCollection() {
        return false;
    }
    
    @Override
    public int size() {
        return isNull() ? 0 : 1;
    }
    
    @Override
    public Value copy() {
        return new PrimitiveValue(value);
    }
    
    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PrimitiveValue that = (PrimitiveValue) obj;
        return Objects.equals(value, that.value) && type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> clazz) {
        if (value == null) {
            return null;
        }
        return (T) value;
    }
    
    public String getStringValue() {
        return value == null ? null : value.toString();
    }
    
    public Integer getIntegerValue() {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
    
    public Long getLongValue() {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    public Double getDoubleValue() {
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }
    
    public Boolean getBooleanValue() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
}
