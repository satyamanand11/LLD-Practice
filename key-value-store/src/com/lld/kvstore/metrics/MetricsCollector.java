package com.lld.kvstore.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsCollector {
    private final AtomicLong totalOperations;
    private final AtomicLong putOperations;
    private final AtomicLong getOperations;
    private final AtomicLong deleteOperations;
    private final AtomicLong errorOperations;
    private final AtomicLong totalLatency;
    private final AtomicDouble averageLatency;
    private final AtomicLong peakThroughput;
    private final AtomicLong currentThroughput;
    private final AtomicLong memoryUsage;
    private final AtomicLong diskUsage;
    private final Map<String, AtomicLong> operationLatencies;
    private final ScheduledExecutorService scheduler;
    private final long startTime;
    
    public MetricsCollector() {
        this.totalOperations = new AtomicLong(0);
        this.putOperations = new AtomicLong(0);
        this.getOperations = new AtomicLong(0);
        this.deleteOperations = new AtomicLong(0);
        this.errorOperations = new AtomicLong(0);
        this.totalLatency = new AtomicLong(0);
        this.averageLatency = new AtomicDouble(0.0);
        this.peakThroughput = new AtomicLong(0);
        this.currentThroughput = new AtomicLong(0);
        this.memoryUsage = new AtomicLong(0);
        this.diskUsage = new AtomicLong(0);
        this.operationLatencies = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.startTime = System.currentTimeMillis();
        
        startMetricsCollection();
    }
    
    public void recordOperation(String operationType, long latencyMs) {
        totalOperations.incrementAndGet();
        totalLatency.addAndGet(latencyMs);
        
        switch (operationType.toUpperCase()) {
            case "PUT":
                putOperations.incrementAndGet();
                break;
            case "GET":
                getOperations.incrementAndGet();
                break;
            case "DELETE":
                deleteOperations.incrementAndGet();
                break;
        }
        
        operationLatencies.computeIfAbsent(operationType, k -> new AtomicLong(0))
                          .addAndGet(latencyMs);
        
        updateAverageLatency();
        updateThroughput();
    }
    
    public void recordError(String operationType) {
        errorOperations.incrementAndGet();
        recordOperation(operationType, 0);
    }
    
    public void recordMemoryUsage(long bytes) {
        memoryUsage.set(bytes);
    }
    
    public void recordDiskUsage(long bytes) {
        diskUsage.set(bytes);
    }
    
    private void updateAverageLatency() {
        long total = totalOperations.get();
        if (total > 0) {
            double avg = (double) totalLatency.get() / total;
            averageLatency.set(avg);
        }
    }
    
    private void updateThroughput() {
        long current = currentThroughput.incrementAndGet();
        long peak = peakThroughput.get();
        if (current > peak) {
            peakThroughput.set(current);
        }
    }
    
    private void startMetricsCollection() {
        scheduler.scheduleAtFixedRate(this::resetCurrentThroughput, 1, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::updateMemoryUsage, 0, 5, TimeUnit.SECONDS);
    }
    
    private void resetCurrentThroughput() {
        currentThroughput.set(0);
    }
    
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        recordMemoryUsage(usedMemory);
    }
    
    public MetricsSnapshot getSnapshot() {
        long uptime = System.currentTimeMillis() - startTime;
        long totalOps = totalOperations.get();
        double opsPerSecond = uptime > 0 ? (double) totalOps / (uptime / 1000.0) : 0.0;
        
        return new MetricsSnapshot(
            totalOps,
            putOperations.get(),
            getOperations.get(),
            deleteOperations.get(),
            errorOperations.get(),
            averageLatency.get(),
            opsPerSecond,
            peakThroughput.get(),
            memoryUsage.get(),
            diskUsage.get(),
            uptime,
            operationLatencies
        );
    }
    
    public void reset() {
        totalOperations.set(0);
        putOperations.set(0);
        getOperations.set(0);
        deleteOperations.set(0);
        errorOperations.set(0);
        totalLatency.set(0);
        averageLatency.set(0.0);
        peakThroughput.set(0);
        currentThroughput.set(0);
        operationLatencies.clear();
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static class MetricsSnapshot {
        private final long totalOperations;
        private final long putOperations;
        private final long getOperations;
        private final long deleteOperations;
        private final long errorOperations;
        private final double averageLatencyMs;
        private final double throughputOpsPerSecond;
        private final long peakThroughput;
        private final long memoryUsageBytes;
        private final long diskUsageBytes;
        private final long uptimeMs;
        private final Map<String, AtomicLong> operationLatencies;
        
        public MetricsSnapshot(long totalOperations, long putOperations, long getOperations,
                             long deleteOperations, long errorOperations, double averageLatencyMs,
                             double throughputOpsPerSecond, long peakThroughput,
                             long memoryUsageBytes, long diskUsageBytes, long uptimeMs,
                             Map<String, AtomicLong> operationLatencies) {
            this.totalOperations = totalOperations;
            this.putOperations = putOperations;
            this.getOperations = getOperations;
            this.deleteOperations = deleteOperations;
            this.errorOperations = errorOperations;
            this.averageLatencyMs = averageLatencyMs;
            this.throughputOpsPerSecond = throughputOpsPerSecond;
            this.peakThroughput = peakThroughput;
            this.memoryUsageBytes = memoryUsageBytes;
            this.diskUsageBytes = diskUsageBytes;
            this.uptimeMs = uptimeMs;
            this.operationLatencies = operationLatencies;
        }
        
        public long getTotalOperations() { return totalOperations; }
        public long getPutOperations() { return putOperations; }
        public long getGetOperations() { return getOperations; }
        public long getDeleteOperations() { return deleteOperations; }
        public long getErrorOperations() { return errorOperations; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public double getThroughputOpsPerSecond() { return throughputOpsPerSecond; }
        public long getPeakThroughput() { return peakThroughput; }
        public long getMemoryUsageBytes() { return memoryUsageBytes; }
        public long getDiskUsageBytes() { return diskUsageBytes; }
        public long getUptimeMs() { return uptimeMs; }
        public Map<String, AtomicLong> getOperationLatencies() { return operationLatencies; }
        
        public double getErrorRate() {
            return totalOperations > 0 ? (double) errorOperations / totalOperations : 0.0;
        }
        
        public double getPutRatio() {
            return totalOperations > 0 ? (double) putOperations / totalOperations : 0.0;
        }
        
        public double getGetRatio() {
            return totalOperations > 0 ? (double) getOperations / totalOperations : 0.0;
        }
        
        public double getDeleteRatio() {
            return totalOperations > 0 ? (double) deleteOperations / totalOperations : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "MetricsSnapshot{totalOps=%d, puts=%d, gets=%d, deletes=%d, errors=%d, " +
                "avgLatency=%.2fms, throughput=%.2f ops/s, peakThroughput=%d, " +
                "memory=%d bytes, disk=%d bytes, uptime=%d ms, errorRate=%.2f%%}",
                totalOperations, putOperations, getOperations, deleteOperations, errorOperations,
                averageLatencyMs, throughputOpsPerSecond, peakThroughput,
                memoryUsageBytes, diskUsageBytes, uptimeMs, getErrorRate() * 100
            );
        }
    }
}
