package com.lld.kvstore.metrics;

import com.lld.kvstore.interfaces.ByteKeyValueStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class MetricsCollector {
    private final ConcurrentHashMap<String, LongAdder> operationCounters;
    private final ConcurrentHashMap<String, LongAdder> latencySum;
    private final ConcurrentHashMap<String, AtomicLong> maxLatency;
    private final ConcurrentHashMap<String, AtomicLong> minLatency;
    private final LongAdder totalOperations;
    private final AtomicLong startTime;
    
    public MetricsCollector() {
        this.operationCounters = new ConcurrentHashMap<>();
        this.latencySum = new ConcurrentHashMap<>();
        this.maxLatency = new ConcurrentHashMap<>();
        this.minLatency = new ConcurrentHashMap<>();
        this.totalOperations = new LongAdder();
        this.startTime = new AtomicLong(System.currentTimeMillis());
    }
    
    public void recordOperation(String operation, long latencyMs) {
        operationCounters.computeIfAbsent(operation, k -> new LongAdder()).increment();
        latencySum.computeIfAbsent(operation, k -> new LongAdder()).add(latencyMs);
        
        maxLatency.computeIfAbsent(operation, k -> new AtomicLong(0))
                  .updateAndGet(current -> Math.max(current, latencyMs));
        
        minLatency.computeIfAbsent(operation, k -> new AtomicLong(Long.MAX_VALUE))
                  .updateAndGet(current -> Math.min(current, latencyMs));
        
        totalOperations.increment();
    }
    
    public void recordThroughput(long operations) {
        totalOperations.add(operations);
    }
    
    public ByteKeyValueStore.Metrics getMetrics() {
        long currentTime = System.currentTimeMillis();
        long uptimeMs = currentTime - startTime.get();
        double uptimeSeconds = uptimeMs / 1000.0;
        
        long totalOps = totalOperations.sum();
        double throughput = uptimeSeconds > 0 ? totalOps / uptimeSeconds : 0.0;
        
        double avgLatency = 0.0;
        if (totalOps > 0) {
            long totalLatency = latencySum.values().stream()
                .mapToLong(LongAdder::sum)
                .sum();
            avgLatency = (double) totalLatency / totalOps;
        }
        
        long putOps = operationCounters.getOrDefault("PUT", new LongAdder()).sum();
        long getOps = operationCounters.getOrDefault("GET", new LongAdder()).sum();
        long deleteOps = operationCounters.getOrDefault("DELETE", new LongAdder()).sum();
        
        long memoryUsage = estimateMemoryUsage();
        
        long diskUsage = estimateDiskUsage();
        
        return new ByteKeyValueStore.Metrics(
            totalOps, putOps, getOps, deleteOps,
            avgLatency, throughput, memoryUsage, diskUsage
        );
    }
    
    public OperationMetrics getOperationMetrics(String operation) {
        long count = operationCounters.getOrDefault(operation, new LongAdder()).sum();
        long totalLatency = latencySum.getOrDefault(operation, new LongAdder()).sum();
        long max = maxLatency.getOrDefault(operation, new AtomicLong(0)).get();
        long min = minLatency.getOrDefault(operation, new AtomicLong(Long.MAX_VALUE)).get();
        
        double avgLatency = count > 0 ? (double) totalLatency / count : 0.0;
        
        return new OperationMetrics(operation, count, avgLatency, min, max);
    }
    
    public java.util.Map<String, OperationMetrics> getAllOperationMetrics() {
        java.util.Map<String, OperationMetrics> metrics = new java.util.HashMap<>();
        
        for (String operation : operationCounters.keySet()) {
            metrics.put(operation, getOperationMetrics(operation));
        }
        
        return metrics;
    }
    
    public void reset() {
        operationCounters.clear();
        latencySum.clear();
        maxLatency.clear();
        minLatency.clear();
        totalOperations.reset();
        startTime.set(System.currentTimeMillis());
    }
    
    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime.get();
    }
    
    public long getTotalOperations() {
        return totalOperations.sum();
    }
    
    public double getCurrentThroughput() {
        long uptimeMs = getUptimeMs();
        if (uptimeMs == 0) return 0.0;
        
        return (double) getTotalOperations() / (uptimeMs / 1000.0);
    }
    
    private long estimateMemoryUsage() {
        
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    private long estimateDiskUsage() {
        
        return 0; 
    }
    
    public static class OperationMetrics {
        private final String operation;
        private final long count;
        private final double avgLatencyMs;
        private final long minLatencyMs;
        private final long maxLatencyMs;
        
        public OperationMetrics(String operation, long count, double avgLatencyMs, 
                              long minLatencyMs, long maxLatencyMs) {
            this.operation = operation;
            this.count = count;
            this.avgLatencyMs = avgLatencyMs;
            this.minLatencyMs = minLatencyMs;
            this.maxLatencyMs = maxLatencyMs;
        }
        
        public String getOperation() { return operation; }
        public long getCount() { return count; }
        public double getAvgLatencyMs() { return avgLatencyMs; }
        public long getMinLatencyMs() { return minLatencyMs; }
        public long getMaxLatencyMs() { return maxLatencyMs; }
        
        @Override
        public String toString() {
            return String.format("OperationMetrics{operation='%s', count=%d, " +
                               "avgLatency=%.2fms, minLatency=%dms, maxLatency=%dms}",
                               operation, count, avgLatencyMs, minLatencyMs, maxLatencyMs);
        }
    }
    
    public static class SystemMetrics {
        private final long totalMemory;
        private final long freeMemory;
        private final long usedMemory;
        private final int availableProcessors;
        private final double systemLoadAverage;
        
        public SystemMetrics() {
            Runtime runtime = Runtime.getRuntime();
            this.totalMemory = runtime.totalMemory();
            this.freeMemory = runtime.freeMemory();
            this.usedMemory = totalMemory - freeMemory;
            this.availableProcessors = runtime.availableProcessors();
            this.systemLoadAverage = -1; 
        }
        
        public long getTotalMemory() { return totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public long getUsedMemory() { return usedMemory; }
        public int getAvailableProcessors() { return availableProcessors; }
        public double getSystemLoadAverage() { return systemLoadAverage; }
        
        @Override
        public String toString() {
            return String.format("SystemMetrics{totalMemory=%d bytes, freeMemory=%d bytes, " +
                               "usedMemory=%d bytes, processors=%d, loadAverage=%.2f}",
                               totalMemory, freeMemory, usedMemory, availableProcessors, systemLoadAverage);
        }
    }
}
