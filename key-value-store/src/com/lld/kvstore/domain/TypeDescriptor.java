package com.lld.kvstore.domain;

public class TypeDescriptor {
    private final HolderType holder;
    private final PrimitiveKind primitive;
    
    public TypeDescriptor(HolderType holder, PrimitiveKind primitive) {
        this.holder = holder;
        this.primitive = primitive;
    }
    
    public HolderType holderType() {
        return holder;
    }
    
    public PrimitiveKind primitiveKind() {
        return primitive;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TypeDescriptor that = (TypeDescriptor) obj;
        return holder == that.holder && primitive == that.primitive;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(holder, primitive);
    }
    
    @Override
    public String toString() {
        return "TypeDescriptor{" +
                "holder=" + holder +
                ", primitive=" + primitive +
                '}';
    }
}
