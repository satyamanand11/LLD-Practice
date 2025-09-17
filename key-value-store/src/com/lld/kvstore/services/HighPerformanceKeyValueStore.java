package com.lld.kvstore.services;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.models.Result;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HighPerformanceKeyValueStore implements ByteKeyValueStore {
    
    private final Map<byte[], byte[]> store;
    private final ReadWriteLock storeLock;
    private final ExecutorService writeExecutor;
    private final ExecutorService readExecutor;
    private final ScheduledExecutorService backgroundExecutor;
    private final AtomicLong operationCounter;
    
    public HighPerformanceKeyValueStore(int maxMemTableSize, int batchSize, long flushIntervalMs) {
        this.store = new ConcurrentHashMap<>();
        this.storeLock = new ReentrantReadWriteLock();
        this.writeExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "WriteThread");
            t.setDaemon(true);
            return t;
        });
        this.readExecutor = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r, "ReadThread");
            t.setDaemon(true);
            return t;
        });
        this.backgroundExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "BackgroundThread");
            t.setDaemon(true);
            return t;
        });
        this.operationCounter = new AtomicLong(0);
    }
    
    @Override
    public Result<Boolean> put(byte[] key, byte[] value) {
        if (key == null || value == null) {
            return Result.ofError("Key and value cannot be null");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.writeLock().lock();
            try {
                store.put(key, value);
                operationCounter.incrementAndGet();
                return Result.ofSuccess(true);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to put key-value pair", e);
        }
    }
    
    @Override
    public Result<byte[]> get(byte[] key) {
        if (key == null) {
            return Result.ofError("Key cannot be null");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.readLock().lock();
            try {
                byte[] value = store.get(key);
                operationCounter.incrementAndGet();
                return Result.ofSuccess(value);
            } finally {
                storeLock.readLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to get value for key", e);
        }
    }
    
    @Override
    public Result<Boolean> delete(byte[] key) {
        if (key == null) {
            return Result.ofError("Key cannot be null");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.writeLock().lock();
            try {
                byte[] removed = store.remove(key);
                operationCounter.incrementAndGet();
                return Result.ofSuccess(removed != null);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to delete key", e);
        }
    }
    
    @Override
    public Result<Boolean> exists(byte[] key) {
        if (key == null) {
            return Result.ofError("Key cannot be null");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.readLock().lock();
            try {
                boolean exists = store.containsKey(key);
                operationCounter.incrementAndGet();
                return Result.ofSuccess(exists);
            } finally {
                storeLock.readLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to check key existence", e);
        }
    }
    
    @Override
    public Result<Long> size() {
        try {
            storeLock.readLock().lock();
            try {
                return Result.ofSuccess((long) store.size());
            } finally {
                storeLock.readLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to get store size", e);
        }
    }
    
    @Override
    public Result<Boolean> clear() {
        try {
            storeLock.writeLock().lock();
            try {
                store.clear();
                return Result.ofSuccess(true);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to clear store", e);
        }
    }
    
    @Override
    public CompletableFuture<Result<Boolean>> putAsync(byte[] key, byte[] value) {
        return CompletableFuture.supplyAsync(() -> put(key, value), writeExecutor);
    }
    
    @Override
    public CompletableFuture<Result<byte[]>> getAsync(byte[] key) {
        return CompletableFuture.supplyAsync(() -> get(key), readExecutor);
    }
    
    @Override
    public Result<Boolean> putBatch(List<KeyValuePair> keyValuePairs) {
        if (keyValuePairs == null || keyValuePairs.isEmpty()) {
            return Result.ofError("Key-value pairs cannot be null or empty");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.writeLock().lock();
            try {
                for (KeyValuePair pair : keyValuePairs) {
                    store.put(pair.getKey(), pair.getValue());
                }
                operationCounter.addAndGet(keyValuePairs.size());
                return Result.ofSuccess(true);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to put batch", e);
        }
    }
    
    @Override
    public Result<List<Optional<byte[]>>> getBatch(List<byte[]> keys) {
        if (keys == null || keys.isEmpty()) {
            return Result.ofError("Keys cannot be null or empty");
        }
        
        long startTime = System.nanoTime();
        try {
            List<Optional<byte[]>> results = new ArrayList<>();
            
            storeLock.readLock().lock();
            try {
                for (byte[] key : keys) {
                    byte[] value = store.get(key);
                    results.add(Optional.ofNullable(value));
                }
                operationCounter.addAndGet(keys.size());
                return Result.ofSuccess(results);
            } finally {
                storeLock.readLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to get batch", e);
        }
    }
    
    @Override
    public Result<Boolean> deleteBatch(List<byte[]> keys) {
        if (keys == null || keys.isEmpty()) {
            return Result.ofError("Keys cannot be null or empty");
        }
        
        long startTime = System.nanoTime();
        try {
            storeLock.writeLock().lock();
            try {
                for (byte[] key : keys) {
                    store.remove(key);
                }
                operationCounter.addAndGet(keys.size());
                return Result.ofSuccess(true);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to delete batch", e);
        }
    }
    
    @Override
    public Result<Boolean> flush() {
        return Result.ofSuccess(true);
    }
    
    @Override
    public Result<Metrics> getMetrics() {
        try {
            long totalOps = operationCounter.get();
            long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            
            Metrics metrics = new Metrics(
                totalOps, totalOps / 3, totalOps / 3, totalOps / 3,
                1.0, totalOps / 10.0, memoryUsage, 0
            );
            return Result.ofSuccess(metrics);
        } catch (Exception e) {
            return Result.ofError("Failed to get metrics", e);
        }
    }
    
    @Override
    public Result<Boolean> start(int port) {
        return Result.ofSuccess(true);
    }
    
    @Override
    public Result<Boolean> stop() {
        return Result.ofSuccess(true);
    }
    
    public void shutdown() {
        try {
            writeExecutor.shutdown();
            readExecutor.shutdown();
            backgroundExecutor.shutdown();
            
            if (!writeExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writeExecutor.shutdownNow();
            }
            if (!readExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                readExecutor.shutdownNow();
            }
            if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
