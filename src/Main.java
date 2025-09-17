package com.lld.kvstore;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.services.HighPerformanceKeyValueStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        HighPerformanceKeyValueStore store = new HighPerformanceKeyValueStore(1000, 100, 1000);
        
        System.out.println("=== Key-Value Store Demo ===");
        
        basicOperations(store);
        batchOperations(store);
        asyncOperations(store);
        concurrencyTest(store);
        performanceTest(store);
        
        store.shutdown();
    }
    
    private static void basicOperations(ByteKeyValueStore store) {
        System.out.println("\n--- Basic Operations ---");
        
        byte[] key1 = "user:1".getBytes();
        byte[] value1 = "John Doe".getBytes();
        
        System.out.println("Put: " + store.put(key1, value1));
        System.out.println("Get: " + new String(store.get(key1).getData().orElse(new byte[0])));
        System.out.println("Exists: " + store.exists(key1));
        System.out.println("Size: " + store.size());
        System.out.println("Delete: " + store.delete(key1));
        System.out.println("Get after delete: " + store.get(key1));
    }
    
    private static void batchOperations(ByteKeyValueStore store) {
        System.out.println("\n--- Batch Operations ---");
        
        List<ByteKeyValueStore.KeyValuePair> pairs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            pairs.add(new ByteKeyValueStore.KeyValuePair(
                ("batch:" + i).getBytes(),
                ("value" + i).getBytes()
            ));
        }
        
        System.out.println("Batch Put: " + store.putBatch(pairs));
        
        List<byte[]> keys = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            keys.add(("batch:" + i).getBytes());
        }
        
        List<java.util.Optional<byte[]>> results = store.getBatch(keys).getData().orElse(new ArrayList<>());
        System.out.println("Batch Get results: " + results.size());
        
        System.out.println("Batch Delete: " + store.deleteBatch(keys));
    }
    
    private static void asyncOperations(ByteKeyValueStore store) {
        System.out.println("\n--- Async Operations ---");
        
        byte[] key = "async:1".getBytes();
        byte[] value = "Async Value".getBytes();
        
        CompletableFuture<ByteKeyValueStore.Result<Boolean>> putFuture = store.putAsync(key, value);
        CompletableFuture<ByteKeyValueStore.Result<byte[]>> getFuture = store.getAsync(key);
        
        putFuture.thenAccept(result -> System.out.println("Async Put: " + result));
        getFuture.thenAccept(result -> System.out.println("Async Get: " + new String(result.getData().orElse(new byte[0]))));
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void concurrencyTest(ByteKeyValueStore store) {
        System.out.println("\n--- Concurrency Test ---");
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                byte[] key = ("concurrent:" + threadId).getBytes();
                byte[] value = ("Value from thread " + threadId).getBytes();
                
                store.put(key, value);
                store.get(key);
                store.exists(key);
            }, executor);
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("Concurrent operations completed");
        
        executor.shutdown();
    }
    
    private static void performanceTest(ByteKeyValueStore store) {
        System.out.println("\n--- Performance Test ---");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 1000; i++) {
            byte[] key = ("perf:" + i).getBytes();
            byte[] value = ("Performance test value " + i).getBytes();
            store.put(key, value);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("1000 puts completed in: " + (endTime - startTime) + "ms");
        
        ByteKeyValueStore.Result<ByteKeyValueStore.Metrics> metricsResult = store.getMetrics();
        if (metricsResult.isSuccess()) {
            System.out.println("Metrics: " + metricsResult.getData().orElse(null));
        }
    }
}
