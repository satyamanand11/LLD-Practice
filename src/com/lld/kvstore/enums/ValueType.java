package com.lld.kvstore.enums;

public enum ValueType {
    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    FLOAT,
    BOOLEAN,
    LIST,
    SET,
    MAP,
    NULL;
    
    public boolean isPrimitive() {
        return this == STRING || this == INTEGER || this == LONG || 
               this == DOUBLE || this == FLOAT || this == BOOLEAN;
    }
    
    public boolean isCollection() {
        return this == LIST || this == SET || this == MAP;
    }
    
    public ValueType getElementType() {
        if (!isCollection()) {
            return null;
        }
        return STRING;
    }
    
    public static ValueType fromObject(Object obj) {
        if (obj == null) {
            return NULL;
        }
        
        if (obj instanceof String) {
            return STRING;
        } else if (obj instanceof Integer) {
            return INTEGER;
        } else if (obj instanceof Long) {
            return LONG;
        } else if (obj instanceof Double) {
            return DOUBLE;
        } else if (obj instanceof Float) {
            return FLOAT;
        } else if (obj instanceof Boolean) {
            return BOOLEAN;
        } else if (obj instanceof java.util.List) {
            return LIST;
        } else if (obj instanceof java.util.Set) {
            return SET;
        } else if (obj instanceof java.util.Map) {
            return MAP;
        }
        
        throw new IllegalArgumentException("Unsupported value type: " + obj.getClass());
    }
}
