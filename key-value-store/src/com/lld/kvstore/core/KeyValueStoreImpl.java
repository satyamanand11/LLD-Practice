package com.lld.kvstore.core;

import com.lld.kvstore.types.*;
import com.lld.kvstore.storage.*;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

public class KeyValueStoreImpl implements KeyValueStore {
    private final Storage storage;
    private final TypeValidator typeValidator;
    private final ValueFactory valueFactory;
    private final ConcurrencyManager concurrencyManager;
    
    public KeyValueStoreImpl(Storage storage, TypeValidator typeValidator, 
                            ValueFactory valueFactory, ConcurrencyManager concurrencyManager) {
        this.storage = storage;
        this.typeValidator = typeValidator;
        this.valueFactory = valueFactory;
        this.concurrencyManager = concurrencyManager;
    }
    
    @Override
    public Result<Void> setPrimitive(String key, Object value) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = typeValidator.createPrimitiveType(value);
            StorageEntry existing = storage.read(key).orElse(null);
            typeValidator.validateTypeCompatibility(existing, type);
            
            Value primitiveValue = valueFactory.createPrimitive(value, type.getPrimitiveType());
            StorageEntry entry = new StorageEntry(key, type, primitiveValue);
            storage.write(key, entry);
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set primitive: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public <T> Result<Void> setList(String key, Collection<T> values) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = typeValidator.createCollectionType(ValueType.LIST, values);
            StorageEntry existing = storage.read(key).orElse(null);
            typeValidator.validateTypeCompatibility(existing, type);
            
            Value listValue = valueFactory.createList(values, type.getPrimitiveType());
            StorageEntry entry = new StorageEntry(key, type, listValue);
            storage.write(key, entry);
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set list: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public <T> Result<Void> setSet(String key, Collection<T> values) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = typeValidator.createCollectionType(ValueType.SET, values);
            StorageEntry existing = storage.read(key).orElse(null);
            typeValidator.validateTypeCompatibility(existing, type);
            
            Value setValue = valueFactory.createSet(values, type.getPrimitiveType());
            StorageEntry entry = new StorageEntry(key, type, setValue);
            storage.write(key, entry);
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set set: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Result<Value> get(String key) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.readLock().lock();
        try {
            return storage.read(key)
                .map(entry -> Result.<Value>success((Value) entry.getValue()))
                .orElse(Result.error("Key not found: " + key));
        } catch (Exception e) {
            return Result.error("Failed to get value: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Result<Void> deleteKey(String key) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            storage.delete(key);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to delete key: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public <T> Result<Void> addToCollection(String key, Collection<T> values) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            StorageEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getValue();
            if (currentValue instanceof ListValue) {
                ListValue listValue = (ListValue) currentValue;
                for (T value : values) {
                    listValue.add(value);
                }
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                for (T value : values) {
                    setValue.add(value);
                }
            } else {
                return Result.error("Key does not contain a collection");
            }
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to add to collection: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public <T> Result<Collection<T>> fetchFromCollection(String key, int limit) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.readLock().lock();
        try {
            StorageEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getValue();
            Collection<T> result;
            
            if (currentValue instanceof ListValue) {
                ListValue listValue = (ListValue) currentValue;
                result = (Collection<T>) listValue.getValues();
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                result = (Collection<T>) setValue.getValues();
            } else {
                return Result.error("Key does not contain a collection");
            }
            
            if (limit > 0 && result.size() > limit) {
                result = result.stream().limit(limit).collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to fetch from collection: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public <T> Result<Void> removeFromCollection(String key, Collection<T> values) {
        ReadWriteLock lock = concurrencyManager.getLock(key);
        lock.writeLock().lock();
        try {
            StorageEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getValue();
            if (currentValue instanceof ListValue) {
                ListValue listValue = (ListValue) currentValue;
                for (T value : values) {
                    listValue.remove(value);
                }
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                for (T value : values) {
                    setValue.remove(value);
                }
            } else {
                return Result.error("Key does not contain a collection");
            }
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to remove from collection: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
}