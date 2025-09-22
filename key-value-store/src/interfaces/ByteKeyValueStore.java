package com.lld.kvstore.interfaces;

import com.lld.kvstore.com.lld.kvstore.models.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ByteKeyValueStore {
    Result<Boolean> put(byte[] key, byte[] value);
    Result<byte[]> get(byte[] key);
    Result<Boolean> delete(byte[] key);
    Result<Boolean> exists(byte[] key);
    Result<Long> size();
    Result<Boolean> clear();
    CompletableFuture<Result<Boolean>> putAsync(byte[] key, byte[] value);
    CompletableFuture<Result<byte[]>> getAsync(byte[] key);
    Result<Boolean> putBatch(List<KeyValuePair> keyValuePairs);
    Result<List<Optional<byte[]>>> getBatch(List<byte[]> keys);
    Result<Boolean> deleteBatch(List<byte[]> keys);
    Result<Boolean> flush();
    Result<Metrics> getMetrics();
    Result<Boolean> start(int port);
    Result<Boolean> stop();
    
    class KeyValuePair {
        private final byte[] key;
        private final byte[] value;
        
        public KeyValuePair(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }
        
        public byte[] getKey() {
            return key;
        }
        
        public byte[] getValue() {
            return value;
        }
    }
    
    class Metrics {
        private final long totalOperations;
        private final long putOperations;
        private final long getOperations;
        private final long deleteOperations;
        private final double averageLatencyMs;
        private final double throughputOpsPerSecond;
        private final long memoryUsageBytes;
        private final long diskUsageBytes;
        
        public Metrics(long totalOperations, long putOperations, long getOperations, 
                      long deleteOperations, double averageLatencyMs, 
                      double throughputOpsPerSecond, long memoryUsageBytes, 
                      long diskUsageBytes) {
            this.totalOperations = totalOperations;
            this.putOperations = putOperations;
            this.getOperations = getOperations;
            this.deleteOperations = deleteOperations;
            this.averageLatencyMs = averageLatencyMs;
            this.throughputOpsPerSecond = throughputOpsPerSecond;
            this.memoryUsageBytes = memoryUsageBytes;
            this.diskUsageBytes = diskUsageBytes;
        }
        
        public long getTotalOperations() { return totalOperations; }
        public long getPutOperations() { return putOperations; }
        public long getGetOperations() { return getOperations; }
        public long getDeleteOperations() { return deleteOperations; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public double getThroughputOpsPerSecond() { return throughputOpsPerSecond; }
        public long getMemoryUsageBytes() { return memoryUsageBytes; }
        public long getDiskUsageBytes() { return diskUsageBytes; }
        
        @Override
        public String toString() {
            return String.format(
                "Metrics{totalOps=%d, puts=%d, gets=%d, deletes=%d, " +
                "avgLatency=%.2fms, throughput=%.2f ops/s, memory=%d bytes, disk=%d bytes}",
                totalOperations, putOperations, getOperations, deleteOperations,
                averageLatencyMs, throughputOpsPerSecond, memoryUsageBytes, diskUsageBytes
            );
        }
    }
}
