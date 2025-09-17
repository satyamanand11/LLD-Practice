package com.lld.kvstore.replication;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.services.HighPerformanceKeyValueStore.Operation;
import com.lld.kvstore.services.HighPerformanceKeyValueStore.OperationType;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplicationManager {
    private final List<ReplicaNode> replicas;
    private final AtomicBoolean isMaster;
    private final ExecutorService replicationExecutor;
    private final BlockingQueue<Operation> replicationQueue;
    private final Map<String, Long> lastReplicatedSequence;
    
    public ReplicationManager() {
        this.replicas = new CopyOnWriteArrayList<>();
        this.isMaster = new AtomicBoolean(true); 
        this.replicationExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "ReplicationThread");
            t.setDaemon(true);
            return t;
        });
        this.replicationQueue = new LinkedBlockingQueue<>();
        this.lastReplicatedSequence = new ConcurrentHashMap<>();
        
        replicationExecutor.submit(this::replicationWorker);
    }
    
    public void replicate(Operation operation) {
        if (!isMaster.get()) {
            return; 
        }
        
        replicationQueue.offer(operation);
    }
    
    public void replicateBatch(List<ByteKeyValueStore.KeyValuePair> pairs, OperationType type) {
        if (!isMaster.get()) {
            return; 
        }
        
        for (ByteKeyValueStore.KeyValuePair pair : pairs) {
            Operation operation = new Operation(type, pair.getKey(), pair.getValue());
            replicationQueue.offer(operation);
        }
    }
    
    public void addReplica(ReplicaNode replica) {
        replicas.add(replica);
        System.out.println("Added replica: " + replica.getAddress());
    }
    
    public void removeReplica(ReplicaNode replica) {
        replicas.remove(replica);
        System.out.println("Removed replica: " + replica.getAddress());
    }
    
    public void promoteToMaster() {
        isMaster.set(true);
        System.out.println("Promoted to master");
    }
    
    public void demoteToSlave() {
        isMaster.set(false);
        System.out.println("Demoted to slave");
    }
    
    public void syncWithMaster(ReplicaNode master) {
        if (isMaster.get()) {
            return; 
        }
        
        try {
            
            System.out.println("Syncing with master: " + master.getAddress());
            
        } catch (Exception e) {
            System.err.println("Failed to sync with master: " + e.getMessage());
        }
    }
    
    public List<ReplicaNode> getReplicas() {
        return new ArrayList<>(replicas);
    }
    
    public boolean isMaster() {
        return isMaster.get();
    }
    
    public ReplicationStatus getStatus() {
        return new ReplicationStatus(
            isMaster.get(),
            replicas.size(),
            replicationQueue.size(),
            lastReplicatedSequence.size()
        );
    }
    
    public void shutdown() {
        replicationExecutor.shutdown();
        try {
            if (!replicationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                replicationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            replicationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void replicationWorker() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Operation operation = replicationQueue.take();
                replicateToAllNodes(operation);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in replication worker: " + e.getMessage());
            }
        }
    }
    
    private void replicateToAllNodes(Operation operation) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (ReplicaNode replica : replicas) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    replica.replicate(operation);
                    lastReplicatedSequence.put(replica.getAddress(), operation.getTimestamp());
                } catch (Exception e) {
                    System.err.println("Failed to replicate to " + replica.getAddress() + ": " + e.getMessage());
                }
            }, replicationExecutor);
            
            futures.add(future);
        }
        
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Some replications failed: " + e.getMessage());
        }
    }
    
    public static class ReplicaNode {
        private final String address;
        private final int port;
        private final AtomicBoolean isHealthy;
        private final long lastHeartbeat;
        
        public ReplicaNode(String address, int port) {
            this.address = address;
            this.port = port;
            this.isHealthy = new AtomicBoolean(true);
            this.lastHeartbeat = System.currentTimeMillis();
        }
        
        public void replicate(Operation operation) throws Exception {
            
            if (!isHealthy.get()) {
                throw new Exception("Replica is not healthy");
            }
            
            Thread.sleep(1);
            
            System.out.println("Replicated operation to " + address + ":" + port);
        }
        
        public void setHealthy(boolean healthy) {
            isHealthy.set(healthy);
        }
        
        public boolean isHealthy() {
            return isHealthy.get();
        }
        
        public String getAddress() { return address; }
        public int getPort() { return port; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        
        @Override
        public String toString() {
            return "ReplicaNode{address='" + address + "', port=" + port + 
                   ", healthy=" + isHealthy.get() + "}";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            ReplicaNode that = (ReplicaNode) obj;
            return port == that.port && Objects.equals(address, that.address);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }
    }
    
    public static class ReplicationStatus {
        private final boolean isMaster;
        private final int replicaCount;
        private final int pendingOperations;
        private final int replicatedOperations;
        
        public ReplicationStatus(boolean isMaster, int replicaCount, 
                               int pendingOperations, int replicatedOperations) {
            this.isMaster = isMaster;
            this.replicaCount = replicaCount;
            this.pendingOperations = pendingOperations;
            this.replicatedOperations = replicatedOperations;
        }
        
        public boolean isMaster() { return isMaster; }
        public int getReplicaCount() { return replicaCount; }
        public int getPendingOperations() { return pendingOperations; }
        public int getReplicatedOperations() { return replicatedOperations; }
        
        @Override
        public String toString() {
            return String.format("ReplicationStatus{isMaster=%s, replicas=%d, " +
                               "pendingOps=%d, replicatedOps=%d}",
                               isMaster, replicaCount, pendingOperations, replicatedOperations);
        }
    }
}
