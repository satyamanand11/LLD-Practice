package com.lld.kvstore.replication;

import com.lld.kvstore.interfaces.ByteKeyValueStore;
import com.lld.kvstore.models.Result;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReplicationManager {
    private final ByteKeyValueStore store;
    private final ReplicationRole role;
    private final List<ReplicaNode> replicas;
    private final ExecutorService replicationExecutor;
    private final AtomicBoolean running;
    private final int replicationFactor;
    private final long replicationTimeoutMs;
    
    public ReplicationManager(ByteKeyValueStore store, ReplicationRole role) {
        this(store, role, 3, 5000);
    }
    
    public ReplicationManager(ByteKeyValueStore store, ReplicationRole role, int replicationFactor, long replicationTimeoutMs) {
        this.store = store;
        this.role = role;
        this.replicas = new ArrayList<>();
        this.replicationExecutor = Executors.newFixedThreadPool(replicationFactor);
        this.running = new AtomicBoolean(false);
        this.replicationFactor = replicationFactor;
        this.replicationTimeoutMs = replicationTimeoutMs;
    }
    
    public void start() {
        if (running.compareAndSet(false, true)) {
            System.out.println("Starting replication manager in " + role + " mode");
            
            if (role == ReplicationRole.MASTER) {
                startMasterReplication();
            } else {
                startSlaveReplication();
            }
        }
    }
    
    public void stop() {
        if (running.compareAndSet(true, false)) {
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
    }
    
    public void addReplica(String host, int port) {
        replicas.add(new ReplicaNode(host, port));
    }
    
    public Result<Boolean> replicateWrite(byte[] key, byte[] value) {
        if (role != ReplicationRole.MASTER) {
            return Result.ofError("Only master can initiate replication");
        }
        
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        
        for (ReplicaNode replica : replicas) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendWriteToReplica(replica, key, value);
                } catch (Exception e) {
                    System.err.println("Failed to replicate to " + replica + ": " + e.getMessage());
                    return false;
                }
            }, replicationExecutor);
            futures.add(future);
        }
        
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get(replicationTimeoutMs, TimeUnit.MILLISECONDS);
            
            long successCount = futures.stream().mapToLong(f -> {
                try {
                    return f.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();
            
            boolean success = successCount >= (replicas.size() / 2 + 1);
            return Result.ofSuccess(success);
            
        } catch (TimeoutException e) {
            return Result.ofError("Replication timeout");
        } catch (Exception e) {
            return Result.ofError("Replication failed: " + e.getMessage());
        }
    }
    
    public Result<Boolean> replicateDelete(byte[] key) {
        if (role != ReplicationRole.MASTER) {
            return Result.ofError("Only master can initiate replication");
        }
        
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        
        for (ReplicaNode replica : replicas) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendDeleteToReplica(replica, key);
                } catch (Exception e) {
                    System.err.println("Failed to replicate delete to " + replica + ": " + e.getMessage());
                    return false;
                }
            }, replicationExecutor);
            futures.add(future);
        }
        
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get(replicationTimeoutMs, TimeUnit.MILLISECONDS);
            
            long successCount = futures.stream().mapToLong(f -> {
                try {
                    return f.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            }).sum();
            
            boolean success = successCount >= (replicas.size() / 2 + 1);
            return Result.ofSuccess(success);
            
        } catch (TimeoutException e) {
            return Result.ofError("Replication timeout");
        } catch (Exception e) {
            return Result.ofError("Replication failed: " + e.getMessage());
        }
    }
    
    private boolean sendWriteToReplica(ReplicaNode replica, byte[] key, byte[] value) throws IOException {
        try (Socket socket = new Socket(replica.getHost(), replica.getPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            ReplicationMessage message = new ReplicationMessage(
                ReplicationMessage.Type.WRITE, key, value
            );
            
            byte[] serialized = message.serialize();
            out.writeInt(serialized.length);
            out.write(serialized);
            out.flush();
            
            boolean success = in.readBoolean();
            return success;
        }
    }
    
    private boolean sendDeleteToReplica(ReplicaNode replica, byte[] key) throws IOException {
        try (Socket socket = new Socket(replica.getHost(), replica.getPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            
            ReplicationMessage message = new ReplicationMessage(
                ReplicationMessage.Type.DELETE, key, null
            );
            
            byte[] serialized = message.serialize();
            out.writeInt(serialized.length);
            out.write(serialized);
            out.flush();
            
            boolean success = in.readBoolean();
            return success;
        }
    }
    
    private void startMasterReplication() {
        System.out.println("Master replication started");
    }
    
    private void startSlaveReplication() {
        System.out.println("Slave replication started");
    }
    
    public enum ReplicationRole {
        MASTER, SLAVE
    }
    
    public static class ReplicaNode {
        private final String host;
        private final int port;
        
        public ReplicaNode(String host, int port) {
            this.host = host;
            this.port = port;
        }
        
        public String getHost() { return host; }
        public int getPort() { return port; }
        
        @Override
        public String toString() {
            return host + ":" + port;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ReplicaNode that = (ReplicaNode) obj;
            return port == that.port && Objects.equals(host, that.host);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(host, port);
        }
    }
    
    public static class ReplicationMessage {
        public enum Type { WRITE, DELETE, SYNC }
        
        private final Type type;
        private final byte[] key;
        private final byte[] value;
        private final long timestamp;
        
        public ReplicationMessage(Type type, byte[] key, byte[] value) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        public byte[] serialize() {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 DataOutputStream dos = new DataOutputStream(baos)) {
                
                dos.writeInt(type.ordinal());
                dos.writeLong(timestamp);
                
                int keyLength = key != null ? key.length : 0;
                dos.writeInt(keyLength);
                if (key != null) {
                    dos.write(key);
                }
                
                int valueLength = value != null ? value.length : 0;
                dos.writeInt(valueLength);
                if (value != null) {
                    dos.write(value);
                }
                
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize replication message", e);
            }
        }
        
        public static ReplicationMessage deserialize(byte[] data) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bais)) {
                
                Type type = Type.values()[dis.readInt()];
                long timestamp = dis.readLong();
                
                int keyLength = dis.readInt();
                byte[] key = null;
                if (keyLength > 0) {
                    key = new byte[keyLength];
                    dis.readFully(key);
                }
                
                int valueLength = dis.readInt();
                byte[] value = null;
                if (valueLength > 0) {
                    value = new byte[valueLength];
                    dis.readFully(value);
                }
                
                return new ReplicationMessage(type, key, value);
            } catch (IOException e) {
                throw new RuntimeException("Failed to deserialize replication message", e);
            }
        }
        
        public Type getType() { return type; }
        public byte[] getKey() { return key; }
        public byte[] getValue() { return value; }
        public long getTimestamp() { return timestamp; }
    }
}
