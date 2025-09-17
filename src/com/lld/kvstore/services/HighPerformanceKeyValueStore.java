package com.lld.kvstore.services;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.models.Result;
import com.lld.kvstore.persistence.MemTable;
import com.lld.kvstore.persistence.PersistenceLayer;
import com.lld.kvstore.persistence.WriteAheadLog;
import com.lld.kvstore.network.NetworkService;
import com.lld.kvstore.metrics.MetricsCollector;
import com.lld.kvstore.replication.ReplicationManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HighPerformanceKeyValueStore implements ByteKeyValueStore {
    
    private final PersistenceLayer persistenceLayer;
    private final WriteAheadLog wal;
    private final MemTable memTable;
    private final NetworkService networkService;
    private final MetricsCollector metricsCollector;
    private final ReplicationManager replicationManager;
    
    private final ReadWriteLock storeLock;
    private final ExecutorService writeExecutor;
    private final ExecutorService readExecutor;
    private final ScheduledExecutorService backgroundExecutor;
    
    private final int maxMemTableSize;
    private final int batchSize;
    private final long flushIntervalMs;
    
    private volatile boolean running;
    private final AtomicLong operationCounter;
    private final BlockingQueue<KeyValuePair> writeQueue;
    
    public HighPerformanceKeyValueStore(int maxMemTableSize, int batchSize, long flushIntervalMs) {
        this.maxMemTableSize = maxMemTableSize;
        this.batchSize = batchSize;
        this.flushIntervalMs = flushIntervalMs;
        
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
        
        this.wal = new WriteAheadLog();
        this.memTable = new MemTable(maxMemTableSize);
        this.persistenceLayer = new PersistenceLayer(wal, memTable);
        this.metricsCollector = new MetricsCollector();
        this.replicationManager = new ReplicationManager();
        this.networkService = new NetworkService(this, metricsCollector);
        
        this.operationCounter = new AtomicLong(0);
        this.writeQueue = new LinkedBlockingQueue<>();
        
        startBackgroundTasks();
    }
    
    @Override
    public Result<Boolean> put(byte[] key, byte[] value) {
        if (key == null || value == null) {
            return Result.ofError("Key and value cannot be null");
        }
        
        long startTime = System.nanoTime();
        try {
            
            wal.append(key, value, OperationType.PUT);
            
            memTable.put(key, value);
            
            replicationManager.replicate(new Operation(OperationType.PUT, key, value));
            
            long latency = (System.nanoTime() - startTime) / 1_000_000; 
            metricsCollector.recordOperation("PUT", latency);
            operationCounter.incrementAndGet();
            
            return Result.ofSuccess(true);
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
                
                Optional<byte[]> value = memTable.get(key);
                if (value.isPresent()) {
                    long latency = (System.nanoTime() - startTime) / 1_000_000;
                    metricsCollector.recordOperation("GET", latency);
                    operationCounter.incrementAndGet();
                    return Result.ofSuccess(value.get());
                }
                
                value = persistenceLayer.get(key);
                long latency = (System.nanoTime() - startTime) / 1_000_000;
                metricsCollector.recordOperation("GET", latency);
                operationCounter.incrementAndGet();
                
                return value.map(Result::ofSuccess)
                           .orElse(Result.ofSuccess(null));
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
            
            wal.append(key, null, OperationType.DELETE);
            
            memTable.delete(key);
            
            replicationManager.replicate(new Operation(OperationType.DELETE, key, null));
            
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordOperation("DELETE", latency);
            operationCounter.incrementAndGet();
            
            return Result.ofSuccess(true);
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
                
                if (memTable.get(key).isPresent()) {
                    long latency = (System.nanoTime() - startTime) / 1_000_000;
                    metricsCollector.recordOperation("EXISTS", latency);
                    return Result.ofSuccess(true);
                }
                
                boolean exists = persistenceLayer.exists(key);
                long latency = (System.nanoTime() - startTime) / 1_000_000;
                metricsCollector.recordOperation("EXISTS", latency);
                
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
                long memTableSize = memTable.size();
                long sstableSize = persistenceLayer.size();
                return Result.ofSuccess(memTableSize + sstableSize);
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
                memTable.clear();
                persistenceLayer.clear();
                wal.clear();
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
            
            wal.appendBatch(keyValuePairs, OperationType.PUT);
            
            for (KeyValuePair pair : keyValuePairs) {
                memTable.put(pair.getKey(), pair.getValue());
            }
            
            replicationManager.replicateBatch(keyValuePairs, OperationType.PUT);
            
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordOperation("PUT_BATCH", latency);
            operationCounter.addAndGet(keyValuePairs.size());
            
            return Result.ofSuccess(true);
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
                    
                    Optional<byte[]> value = memTable.get(key);
                    if (value.isPresent()) {
                        results.add(value);
                    } else {
                        
                        value = persistenceLayer.get(key);
                        results.add(value);
                    }
                }
                
                long latency = (System.nanoTime() - startTime) / 1_000_000;
                metricsCollector.recordOperation("GET_BATCH", latency);
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
            
            List<KeyValuePair> tombstones = new ArrayList<>();
            for (byte[] key : keys) {
                tombstones.add(new KeyValuePair(key, null));
            }
            wal.appendBatch(tombstones, OperationType.DELETE);
            
            for (byte[] key : keys) {
                memTable.delete(key);
            }
            
            replicationManager.replicateBatch(tombstones, OperationType.DELETE);
            
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            metricsCollector.recordOperation("DELETE_BATCH", latency);
            operationCounter.addAndGet(keys.size());
            
            return Result.ofSuccess(true);
        } catch (Exception e) {
            return Result.ofError("Failed to delete batch", e);
        }
    }
    
    @Override
    public Result<Boolean> flush() {
        try {
            storeLock.writeLock().lock();
            try {
                persistenceLayer.flush();
                wal.sync();
                return Result.ofSuccess(true);
            } finally {
                storeLock.writeLock().unlock();
            }
        } catch (Exception e) {
            return Result.ofError("Failed to flush", e);
        }
    }
    
    @Override
    public Result<Metrics> getMetrics() {
        try {
            Metrics metrics = metricsCollector.getMetrics();
            return Result.ofSuccess(metrics);
        } catch (Exception e) {
            return Result.ofError("Failed to get metrics", e);
        }
    }
    
    @Override
    public Result<Boolean> start(int port) {
        try {
            if (running) {
                return Result.ofError("Store is already running");
            }
            
            networkService.start(port);
            running = true;
            return Result.ofSuccess(true);
        } catch (Exception e) {
            return Result.ofError("Failed to start store", e);
        }
    }
    
    @Override
    public Result<Boolean> stop() {
        try {
            if (!running) {
                return Result.ofError("Store is not running");
            }
            
            networkService.stop();
            running = false;
            return Result.ofSuccess(true);
        } catch (Exception e) {
            return Result.ofError("Failed to stop store", e);
        }
    }
    
    private void startBackgroundTasks() {
        
        backgroundExecutor.scheduleAtFixedRate(() -> {
            if (memTable.isFull()) {
                try {
                    storeLock.writeLock().lock();
                    try {
                        persistenceLayer.flushMemTable();
                    } finally {
                        storeLock.writeLock().unlock();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to flush memtable: " + e.getMessage());
                }
            }
        }, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
        
        backgroundExecutor.scheduleAtFixedRate(() -> {
            try {
                persistenceLayer.compact();
            } catch (Exception e) {
                System.err.println("Failed to compact: " + e.getMessage());
            }
        }, flushIntervalMs * 2, flushIntervalMs * 2, TimeUnit.MILLISECONDS);
    }
    
    public void shutdown() {
        try {
            if (running) {
                stop();
            }
            
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
    
    public enum OperationType {
        PUT, DELETE
    }
    
    public static class Operation {
        private final OperationType type;
        private final byte[] key;
        private final byte[] value;
        private final long timestamp;
        
        public Operation(OperationType type, byte[] key, byte[] value) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        public OperationType getType() { return type; }
        public byte[] getKey() { return key; }
        public byte[] getValue() { return value; }
        public long getTimestamp() { return timestamp; }
    }
}
