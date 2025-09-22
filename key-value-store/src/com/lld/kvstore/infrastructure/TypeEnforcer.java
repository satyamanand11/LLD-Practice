package com.lld.kvstore.infrastructure;

import com.lld.kvstore.domain.HolderType;
import com.lld.kvstore.domain.PrimitiveKind;
import com.lld.kvstore.domain.TypeDescriptor;
import java.util.Collection;

public class TypeEnforcer {
    
    public TypeDescriptor descriptorForPrimitive(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        PrimitiveKind kind = inferPrimitiveKind(value);
        return new TypeDescriptor(HolderType.PRIMITIVE, kind);
    }
    
    public TypeDescriptor descriptorForCollection(HolderType holder, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Collection cannot be null or empty");
        }
        
        Object firstValue = values.iterator().next();
        PrimitiveKind kind = inferPrimitiveKind(firstValue);
        
        return new TypeDescriptor(holder, kind);
    }
    
    public void ensureCompatible(StoredEntry existing, TypeDescriptor incoming) {
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
    
    private PrimitiveKind inferPrimitiveKind(Object value) {
        if (value instanceof String) {
            return PrimitiveKind.STRING;
        } else if (value instanceof Integer) {
            return PrimitiveKind.INTEGER;
        } else if (value instanceof Long) {
            return PrimitiveKind.LONG;
        } else if (value instanceof Double) {
            return PrimitiveKind.DOUBLE;
        } else if (value instanceof Float) {
            return PrimitiveKind.FLOAT;
        } else if (value instanceof Boolean) {
            return PrimitiveKind.BOOLEAN;
        } else {
            throw new IllegalArgumentException("Unsupported primitive type: " + value.getClass());
        }
    }
}
