package com.lld.kvstore.storage;

import com.lld.kvstore.types.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ValueFactory {
    
    public Value createPrimitive(Object value, PrimitiveType primitiveType) {
        return new PrimitiveValue(value, primitiveType);
    }
    
    public Value createList(Collection<?> values, PrimitiveType primitiveType) {
        return new ListValue((List<Object>) values, primitiveType);
    }
    
    public Value createSet(Collection<?> values, PrimitiveType primitiveType) {
        return new SetValue((Set<Object>) values, primitiveType);
    }
}
