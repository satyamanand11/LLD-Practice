package com.lld.kvstore.infrastructure;

import com.lld.kvstore.domain.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DefaultValueFactory implements ValueFactory {
    
    @Override
    public Value createPrimitive(TypeDescriptor type, Object data) {
        return new PrimitiveValue(data, type.primitiveKind());
    }
    
    @Override
    public Value create(TypeDescriptor type, Collection data) {
        switch (type.holderType()) {
            case LIST:
                return new ListValue((List<Object>) data, type.primitiveKind());
            case SET:
                return new SetValue((Set<Object>) data, type.primitiveKind());
            default:
                throw new IllegalArgumentException("Unsupported holder type: " + type.holderType());
        }
    }
}
