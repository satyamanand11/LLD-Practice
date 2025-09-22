package com.lld.kvstore.infrastructure;

import com.lld.kvstore.domain.TypeDescriptor;
import com.lld.kvstore.domain.Value;
import java.util.Collection;

public interface ValueFactory {
    Value createPrimitive(TypeDescriptor type, Object data);
    Value create(TypeDescriptor type, Collection data);
}
