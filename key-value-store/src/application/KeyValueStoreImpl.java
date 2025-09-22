package com.lld.kvstore.application;

import com.lld.kvstore.api.KeyValueStore;
import com.lld.kvstore.api.Result;
import com.lld.kvstore.domain.SetValue;
import com.lld.kvstore.domain.Value;
import com.lld.kvstore.domain.HolderType;
import com.lld.kvstore.infrastructure.*;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;

public class KeyValueStoreImpl implements KeyValueStore {
    private final Storage storage;
    private final TypeEnforcer enforcer;
    private final ValueFactory factory;
    private final LockManager lockManager;
    
    public KeyValueStoreImpl(Storage storage, TypeEnforcer enforcer, ValueFactory factory, LockManager lockManager) {
        this.storage = storage;
        this.enforcer = enforcer;
        this.factory = factory;
        this.lockManager = lockManager;
    }
    
    @Override
    public Result<Void> setPrimitive(String key, Object value) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = enforcer.descriptorForPrimitive(value);
            StoredEntry existing = storage.read(key).orElse(null);
            enforcer.ensureCompatible(existing, type);
            
            Value primitiveValue = factory.createPrimitive(type, value);
            StoredEntry entry = new StoredEntry(key, type, primitiveValue);
            storage.write(key, entry);
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set primitive: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Result<Void> setList(String key, Collection<?> values) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = enforcer.descriptorForCollection(HolderType.LIST, values);
            StoredEntry existing = storage.read(key).orElse(null);
            enforcer.ensureCompatible(existing, type);
            
            Value listValue = factory.create(type, values);
            StoredEntry entry = new StoredEntry(key, type, listValue);
            storage.write(key, entry);
            
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("Failed to set list: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Result<Void> setSet(String key, Collection<?> values) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.writeLock().lock();
        try {
            TypeDescriptor type = enforcer.descriptorForCollection(HolderType.SET, values);
            StoredEntry existing = storage.read(key).orElse(null);
            enforcer.ensureCompatible(existing, type);
            
            Value setValue = factory.create(type, values);
            StoredEntry entry = new StoredEntry(key, type, setValue);
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
        ReadWriteLock lock = lockManager.forKey(key);
        lock.readLock().lock();
        try {
            return storage.read(key)
                .map(entry -> Result.<Value>success((Value) entry.getPayload()))
                .orElse(Result.error("Key not found: " + key));
        } catch (Exception e) {
            return Result.error("Failed to get value: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public Result<Void> deleteKey(String key) {
        ReadWriteLock lock = lockManager.forKey(key);
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
    public Result<Void> addToCollection(String key, Collection<?> values) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.writeLock().lock();
        try {
            StoredEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getPayload();
            if (currentValue instanceof com.lld.kvstore.domain.ListValue) {
                com.lld.kvstore.domain.ListValue listValue = (com.lld.kvstore.domain.ListValue) currentValue;
                for (Object value : values) {
                    listValue.add(value);
                }
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                for (Object value : values) {
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
    public Result<Collection<?>> fetchFromCollection(String key, int limit) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.readLock().lock();
        try {
            StoredEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getPayload();
            Collection<?> result;
            
            if (currentValue instanceof com.lld.kvstore.domain.ListValue) {
                com.lld.kvstore.domain.ListValue listValue = (com.lld.kvstore.domain.ListValue) currentValue;
                result = listValue.get();
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                result = setValue.get();
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
    public Result<Void> removeFromCollection(String key, Collection<?> values) {
        ReadWriteLock lock = lockManager.forKey(key);
        lock.writeLock().lock();
        try {
            StoredEntry existing = storage.read(key)
                .orElseThrow(() -> new IllegalArgumentException("Key not found: " + key));
            
            Value currentValue = (Value) existing.getPayload();
            if (currentValue instanceof com.lld.kvstore.domain.ListValue) {
                com.lld.kvstore.domain.ListValue listValue = (com.lld.kvstore.domain.ListValue) currentValue;
                for (Object value : values) {
                    listValue.remove(value);
                }
            } else if (currentValue instanceof SetValue) {
                SetValue setValue = (SetValue) currentValue;
                for (Object value : values) {
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
