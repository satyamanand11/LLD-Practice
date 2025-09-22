package com.lld.kvstore.types;

public class PrimitiveValue extends Value {
    private final Object value;
    private final TypeDescriptor typeDescriptor;
    
    public PrimitiveValue(Object value, PrimitiveType primitiveType) {
        this.value = value;
        this.typeDescriptor = new TypeDescriptor(ValueType.PRIMITIVE, primitiveType);
    }
    
    public Object getValue() {
        return value;
    }
    
    @Override
    public TypeDescriptor getType() {
        return typeDescriptor;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PrimitiveValue that = (PrimitiveValue) obj;
        return java.util.Objects.equals(value, that.value) && 
               java.util.Objects.equals(typeDescriptor, that.typeDescriptor);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(value, typeDescriptor);
    }
    
    @Override
    public String toString() {
        return "PrimitiveValue{" +
                "value=" + value +
                ", type=" + typeDescriptor +
                '}';
    }
}
