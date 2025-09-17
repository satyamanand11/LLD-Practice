package com.lld.kvstore.persistence;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemTable {
    private final ConcurrentSkipListMap<byte[], byte[]> data;
    private final AtomicLong size;
    private final long maxSize;
    private final byte[] TOMBSTONE = new byte[0]; 
    
    public MemTable(long maxSize) {
        this.data = new ConcurrentSkipListMap<>(Arrays::compare);
        this.size = new AtomicLong(0);
        this.maxSize = maxSize;
    }
    
    public void put(byte[] key, byte[] value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        byte[] oldValue = data.put(key, value);
        if (oldValue == null) {
            size.incrementAndGet();
        }
    }
    
    public Optional<byte[]> get(byte[] key) {
        if (key == null) {
            return Optional.empty();
        }
        
        byte[] value = data.get(key);
        if (value == null) {
            return Optional.empty();
        }
        
        if (Arrays.equals(value, TOMBSTONE)) {
            return Optional.empty();
        }
        
        return Optional.of(value);
    }
    
    public void delete(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        
        byte[] oldValue = data.put(key, TOMBSTONE);
        if (oldValue == null) {
            size.incrementAndGet();
        }
    }
    
    public boolean containsKey(byte[] key) {
        if (key == null) {
            return false;
        }
        
        byte[] value = data.get(key);
        return value != null && !Arrays.equals(value, TOMBSTONE);
    }
    
    public long size() {
        return size.get();
    }
    
    public boolean isFull() {
        return size.get() >= maxSize;
    }
    
    public boolean isEmpty() {
        return size.get() == 0;
    }
    
    public void clear() {
        data.clear();
        size.set(0);
    }
    
    public Map<byte[], byte[]> snapshot() {
        return new HashMap<>(data);
    }
    
    public Set<byte[]> keySet() {
        return new HashSet<>(data.keySet());
    }
    
    public Collection<byte[]> values() {
        List<byte[]> values = new ArrayList<>();
        for (byte[] value : data.values()) {
            if (!Arrays.equals(value, TOMBSTONE)) {
                values.add(value);
            }
        }
        return values;
    }
    
    public Set<Map.Entry<byte[], byte[]>> entrySet() {
        return new HashSet<>(data.entrySet());
    }
    
    public NavigableMap<byte[], byte[]> subMap(byte[] startKey, byte[] endKey) {
        return data.subMap(startKey, true, endKey, true);
    }
    
    public NavigableMap<byte[], byte[]> headMap(byte[] key) {
        return data.headMap(key, true);
    }
    
    public NavigableMap<byte[], byte[]> tailMap(byte[] key) {
        return data.tailMap(key, true);
    }
    
    public byte[] firstKey() {
        return data.firstKey();
    }
    
    public byte[] lastKey() {
        return data.lastKey();
    }
    
    public Map.Entry<byte[], byte[]> firstEntry() {
        return data.firstEntry();
    }
    
    public Map.Entry<byte[], byte[]> lastEntry() {
        return data.lastEntry();
    }
    
    public Map.Entry<byte[], byte[]> ceilingEntry(byte[] key) {
        return data.ceilingEntry(key);
    }
    
    public Map.Entry<byte[], byte[]> floorEntry(byte[] key) {
        return data.floorEntry(key);
    }
    
    public Map.Entry<byte[], byte[]> higherEntry(byte[] key) {
        return data.higherEntry(key);
    }
    
    public Map.Entry<byte[], byte[]> lowerEntry(byte[] key) {
        return data.lowerEntry(key);
    }
    
    public Iterator<Map.Entry<byte[], byte[]>> iterator() {
        return data.entrySet().iterator();
    }
    
    public Iterator<Map.Entry<byte[], byte[]>> descendingIterator() {
        return data.descendingMap().entrySet().iterator();
    }
    
    public long getMemoryUsage() {
        long totalSize = 0;
        for (Map.Entry<byte[], byte[]> entry : data.entrySet()) {
            totalSize += entry.getKey().length + entry.getValue().length;
        }
        return totalSize;
    }
    
    public MemTableStats getStats() {
        long totalKeys = size.get();
        long deletedKeys = 0;
        long totalKeySize = 0;
        long totalValueSize = 0;
        
        for (Map.Entry<byte[], byte[]> entry : data.entrySet()) {
            totalKeySize += entry.getKey().length;
            totalValueSize += entry.getValue().length;
            if (Arrays.equals(entry.getValue(), TOMBSTONE)) {
                deletedKeys++;
            }
        }
        
        return new MemTableStats(totalKeys, deletedKeys, totalKeySize, totalValueSize);
    }
    
    public static class MemTableStats {
        private final long totalKeys;
        private final long deletedKeys;
        private final long totalKeySize;
        private final long totalValueSize;
        
        public MemTableStats(long totalKeys, long deletedKeys, long totalKeySize, long totalValueSize) {
            this.totalKeys = totalKeys;
            this.deletedKeys = deletedKeys;
            this.totalKeySize = totalKeySize;
            this.totalValueSize = totalValueSize;
        }
        
        public long getTotalKeys() { return totalKeys; }
        public long getDeletedKeys() { return deletedKeys; }
        public long getTotalKeySize() { return totalKeySize; }
        public long getTotalValueSize() { return totalValueSize; }
        public long getTotalSize() { return totalKeySize + totalValueSize; }
        public double getDeletionRatio() { 
            return totalKeys > 0 ? (double) deletedKeys / totalKeys : 0.0; 
        }
        
        @Override
        public String toString() {
            return String.format("MemTableStats{totalKeys=%d, deletedKeys=%d, " +
                               "keySize=%d bytes, valueSize=%d bytes, totalSize=%d bytes, " +
                               "deletionRatio=%.2f%%}",
                               totalKeys, deletedKeys, totalKeySize, totalValueSize, 
                               getTotalSize(), getDeletionRatio() * 100);
        }
    }
}
