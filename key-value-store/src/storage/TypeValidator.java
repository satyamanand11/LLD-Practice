package com.lld.kvstore.storage;

import com.lld.kvstore.types.ValueType;
import com.lld.kvstore.types.PrimitiveType;
import com.lld.kvstore.types.TypeDescriptor;
import java.util.Collection;

public class TypeValidator {
    
    public TypeDescriptor createPrimitiveType(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        PrimitiveType primitiveType = inferPrimitiveType(value);
        return new TypeDescriptor(ValueType.PRIMITIVE, primitiveType);
    }
    
    public TypeDescriptor createCollectionType(ValueType valueType, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Collection cannot be null or empty");
        }
        
        Object firstValue = values.iterator().next();
        PrimitiveType primitiveType = inferPrimitiveType(firstValue);
        
        return new TypeDescriptor(valueType, primitiveType);
    }
    
    public void validateTypeCompatibility(StorageEntry existing, TypeDescriptor incoming) {
        if (existing == null) {
            return;
        }
        
        TypeDescriptor existingType = existing.getType();
        if (!existingType.equals(incoming)) {
            throw new IllegalArgumentException(
                "Type mismatch. Existing: " + existingType + ", Incoming: " + incoming
            );
        }
    }
    
    private PrimitiveType inferPrimitiveType(Object value) {
        if (value instanceof String) {
            return PrimitiveType.STRING;
        } else if (value instanceof Integer) {
            return PrimitiveType.INTEGER;
        } else if (value instanceof Long) {
            return PrimitiveType.LONG;
        } else if (value instanceof Double) {
            return PrimitiveType.DOUBLE;
        } else if (value instanceof Float) {
            return PrimitiveType.FLOAT;
        } else if (value instanceof Boolean) {
            return PrimitiveType.BOOLEAN;
        } else {
            throw new IllegalArgumentException("Unsupported primitive type: " + value.getClass());
        }
    }
}
