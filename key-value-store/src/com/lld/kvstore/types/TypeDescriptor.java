package com.lld.kvstore.types;

public class TypeDescriptor {
    private final ValueType valueType;
    private final PrimitiveType primitiveType;
    
    public TypeDescriptor(ValueType valueType, PrimitiveType primitiveType) {
        this.valueType = valueType;
        this.primitiveType = primitiveType;
    }
    
    public ValueType getValueType() {
        return valueType;
    }
    
    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TypeDescriptor that = (TypeDescriptor) obj;
        return valueType == that.valueType && primitiveType == that.primitiveType;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(valueType, primitiveType);
    }
    
    @Override
    public String toString() {
        return "TypeDescriptor{" +
                "valueType=" + valueType +
                ", primitiveType=" + primitiveType +
                '}';
    }
}
