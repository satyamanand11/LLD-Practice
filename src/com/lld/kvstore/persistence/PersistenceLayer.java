package com.lld.kvstore.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PersistenceLayer {
    private final WriteAheadLog wal;
    private final MemTable memTable;
    private final List<SSTable> sstables;
    private final ReadWriteLock lock;
    private final Path dataDirectory;
    private final int maxSSTableSize;
    private final int maxLevels;
    
    public PersistenceLayer(WriteAheadLog wal, MemTable memTable) {
        this(wal, memTable, "data", 100 * 1024 * 1024, 7); 
    }
    
    public PersistenceLayer(WriteAheadLog wal, MemTable memTable, String dataDir, int maxSSTableSize, int maxLevels) {
        this.wal = wal;
        this.memTable = memTable;
        this.sstables = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
        this.dataDirectory = Paths.get(dataDir);
        this.maxSSTableSize = maxSSTableSize;
        this.maxLevels = maxLevels;
        
        try {
            Files.createDirectories(dataDirectory);
            loadExistingSSTables();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize persistence layer", e);
        }
    }
    
    public void write(byte[] key, byte[] value) throws IOException {
        
        wal.append(key, value, WriteAheadLog.LogEntry.OperationType.PUT);
        
        memTable.put(key, value);
        
        if (memTable.isFull()) {
            flushMemTable();
        }
    }
    
    public Optional<byte[]> get(byte[] key) throws IOException {
        lock.readLock().lock();
        try {
            
            Optional<byte[]> value = memTable.get(key);
            if (value.isPresent()) {
                return value;
            }
            
            for (int i = sstables.size() - 1; i >= 0; i--) {
                SSTable sstable = sstables.get(i);
                if (sstable.contains(key)) {
                    value = sstable.get(key);
                    if (value.isPresent()) {
                        return value;
                    }
                }
            }
            
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void delete(byte[] key) throws IOException {
        
        wal.append(key, null, WriteAheadLog.LogEntry.OperationType.DELETE);
        
        memTable.delete(key);
        
        if (memTable.isFull()) {
            flushMemTable();
        }
    }
    
    public boolean exists(byte[] key) throws IOException {
        lock.readLock().lock();
        try {
            
            if (memTable.containsKey(key)) {
                return true;
            }
            
            for (SSTable sstable : sstables) {
                if (sstable.contains(key)) {
                    return sstable.get(key).isPresent();
                }
            }
            
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public long size() throws IOException {
        lock.readLock().lock();
        try {
            long totalSize = memTable.size();
            for (SSTable sstable : sstables) {
                totalSize += sstable.size();
            }
            return totalSize;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void flushMemTable() throws IOException {
        lock.writeLock().lock();
        try {
            if (memTable.isEmpty()) {
                return;
            }
            
            SSTable newSSTable = SSTable.createFromMemTable(memTable, dataDirectory, sstables.size());
            sstables.add(newSSTable);
            
            memTable.clear();
            
            if (shouldCompact()) {
                compact();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void flush() throws IOException {
        lock.writeLock().lock();
        try {
            flushMemTable();
            wal.sync();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void compact() throws IOException {
        lock.writeLock().lock();
        try {
            List<SSTable> newSSTables = new ArrayList<>();
            
            Map<Integer, List<SSTable>> sstablesByLevel = groupSSTablesByLevel();
            
            for (int level = 0; level < maxLevels; level++) {
                List<SSTable> levelSSTables = sstablesByLevel.getOrDefault(level, new ArrayList<>());
                if (levelSSTables.size() > 10) { 
                    List<SSTable> compacted = compactLevel(levelSSTables, level + 1);
                    newSSTables.addAll(compacted);
                } else {
                    newSSTables.addAll(levelSSTables);
                }
            }
            
            sstables.clear();
            sstables.addAll(newSSTables);
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void clear() throws IOException {
        lock.writeLock().lock();
        try {
            memTable.clear();
            for (SSTable sstable : sstables) {
                sstable.delete();
            }
            sstables.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void loadExistingSSTables() throws IOException {
        if (!Files.exists(dataDirectory)) {
            return;
        }
        
        Files.list(dataDirectory)
            .filter(path -> path.toString().endsWith(".sstable"))
            .sorted()
            .forEach(path -> {
                try {
                    SSTable sstable = SSTable.loadFromFile(path);
                    sstables.add(sstable);
                } catch (IOException e) {
                    System.err.println("Failed to load SSTable: " + path + ", " + e.getMessage());
                }
            });
    }
    
    private Map<Integer, List<SSTable>> groupSSTablesByLevel() {
        Map<Integer, List<SSTable>> grouped = new HashMap<>();
        for (SSTable sstable : sstables) {
            grouped.computeIfAbsent(sstable.getLevel(), k -> new ArrayList<>()).add(sstable);
        }
        return grouped;
    }
    
    private List<SSTable> compactLevel(List<SSTable> levelSSTables, int newLevel) throws IOException {
        
        List<SSTable> compacted = new ArrayList<>();
        
        if (levelSSTables.isEmpty()) {
            return compacted;
        }
        
        SSTable merged = SSTable.merge(levelSSTables, dataDirectory, sstables.size(), newLevel);
        compacted.add(merged);
        
        for (SSTable sstable : levelSSTables) {
            sstable.delete();
        }
        
        return compacted;
    }
    
    private boolean shouldCompact() {
        return sstables.size() > 10; 
    }
    
    public PersistenceStats getStats() throws IOException {
        lock.readLock().lock();
        try {
            long memTableSize = memTable.size();
            long sstableCount = sstables.size();
            long totalSSTableSize = 0;
            
            for (SSTable sstable : sstables) {
                totalSSTableSize += sstable.size();
            }
            
            return new PersistenceStats(memTableSize, sstableCount, totalSSTableSize);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static class PersistenceStats {
        private final long memTableSize;
        private final long sstableCount;
        private final long totalSSTableSize;
        
        public PersistenceStats(long memTableSize, long sstableCount, long totalSSTableSize) {
            this.memTableSize = memTableSize;
            this.sstableCount = sstableCount;
            this.totalSSTableSize = totalSSTableSize;
        }
        
        public long getMemTableSize() { return memTableSize; }
        public long getSSTableCount() { return sstableCount; }
        public long getTotalSSTableSize() { return totalSSTableSize; }
        public long getTotalSize() { return memTableSize + totalSSTableSize; }
        
        @Override
        public String toString() {
            return String.format("PersistenceStats{memTableSize=%d, sstableCount=%d, " +
                               "sstableSize=%d bytes, totalSize=%d bytes}",
                               memTableSize, sstableCount, totalSSTableSize, getTotalSize());
        }
    }
}
