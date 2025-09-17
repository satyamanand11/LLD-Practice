package com.lld.kvstore.persistence;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class SSTable {
    private final Path dataFile;
    private final Path indexFile;
    private final int level;
    private final long size;
    private final byte[] minKey;
    private final byte[] maxKey;
    private final Map<byte[], Long> index; 
    private final byte[] TOMBSTONE = new byte[0];
    
    private SSTable(Path dataFile, Path indexFile, int level, long size, 
                   byte[] minKey, byte[] maxKey, Map<byte[], Long> index) {
        this.dataFile = dataFile;
        this.indexFile = indexFile;
        this.level = level;
        this.size = size;
        this.minKey = minKey;
        this.maxKey = maxKey;
        this.index = index;
    }
    
    public static SSTable createFromMemTable(MemTable memTable, Path dataDirectory, int sequenceNumber) throws IOException {
        return createFromMemTable(memTable, dataDirectory, sequenceNumber, 0);
    }
    
    public static SSTable createFromMemTable(MemTable memTable, Path dataDirectory, int sequenceNumber, int level) throws IOException {
        String fileName = String.format("sstable_%d_%d", level, sequenceNumber);
        Path dataFile = dataDirectory.resolve(fileName + ".data");
        Path indexFile = dataDirectory.resolve(fileName + ".index");
        
        Map<byte[], Long> index = new HashMap<>();
        byte[] minKey = null;
        byte[] maxKey = null;
        long filePosition = 0;
        
        try (FileChannel dataChannel = FileChannel.open(dataFile, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileChannel indexChannel = FileChannel.open(indexFile, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            ByteBuffer dataBuffer = ByteBuffer.allocate(64 * 1024); 
            ByteBuffer indexBuffer = ByteBuffer.allocate(32 * 1024); 
            
            for (Map.Entry<byte[], byte[]> entry : memTable.entrySet()) {
                byte[] key = entry.getKey();
                byte[] value = entry.getValue();
                
                if (minKey == null || Arrays.compare(key, minKey) < 0) {
                    minKey = key;
                }
                if (maxKey == null || Arrays.compare(key, maxKey) > 0) {
                    maxKey = key;
                }
                
                int keyLength = key.length;
                int valueLength = value.length;
                int totalLength = 4 + keyLength + 4 + valueLength; 
                
                if (dataBuffer.remaining() < totalLength) {
                    dataBuffer.flip();
                    dataChannel.write(dataBuffer);
                    dataBuffer.clear();
                }
                
                dataBuffer.putInt(keyLength);
                dataBuffer.put(key);
                dataBuffer.putInt(valueLength);
                dataBuffer.put(value);
                
                index.put(key, filePosition);
                filePosition += totalLength;
                
                if (indexBuffer.remaining() < 4 + keyLength + 8) {
                    indexBuffer.flip();
                    indexChannel.write(indexBuffer);
                    indexBuffer.clear();
                }
                
                indexBuffer.putInt(keyLength);
                indexBuffer.put(key);
                indexBuffer.putLong(filePosition - totalLength);
            }
            
            dataBuffer.flip();
            dataChannel.write(dataBuffer);
            indexBuffer.flip();
            indexChannel.write(indexBuffer);
        }
        
        return new SSTable(dataFile, indexFile, level, filePosition, minKey, maxKey, index);
    }
    
    public static SSTable loadFromFile(Path dataFile) throws IOException {
        Path indexFile = Paths.get(dataFile.toString().replace(".data", ".index"));
        
        if (!Files.exists(indexFile)) {
            throw new IOException("Index file not found: " + indexFile);
        }
        
        Map<byte[], Long> index = new HashMap<>();
        byte[] minKey = null;
        byte[] maxKey = null;
        long size = 0;
        int level = 0;
        
        String fileName = dataFile.getFileName().toString();
        String[] parts = fileName.replace(".data", "").split("_");
        if (parts.length >= 2) {
            try {
                level = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                
            }
        }
        
        try (FileChannel indexChannel = FileChannel.open(indexFile, StandardOpenOption.READ)) {
            ByteBuffer indexBuffer = ByteBuffer.allocate(32 * 1024);
            long indexPosition = 0;
            
            while (indexPosition < indexChannel.size()) {
                indexBuffer.clear();
                int bytesRead = indexChannel.read(indexBuffer, indexPosition);
                if (bytesRead <= 0) break;
                
                indexBuffer.flip();
                while (indexBuffer.hasRemaining()) {
                    int keyLength = indexBuffer.getInt();
                    byte[] key = new byte[keyLength];
                    indexBuffer.get(key);
                    long dataPosition = indexBuffer.getLong();
                    
                    index.put(key, dataPosition);
                    
                    if (minKey == null || Arrays.compare(key, minKey) < 0) {
                        minKey = key;
                    }
                    if (maxKey == null || Arrays.compare(key, maxKey) > 0) {
                        maxKey = key;
                    }
                }
                
                indexPosition += bytesRead;
            }
        }
        
        size = Files.size(dataFile);
        
        return new SSTable(dataFile, indexFile, level, size, minKey, maxKey, index);
    }
    
    public static SSTable merge(List<SSTable> sstables, Path dataDirectory, int sequenceNumber, int level) throws IOException {
        String fileName = String.format("sstable_%d_%d", level, sequenceNumber);
        Path dataFile = dataDirectory.resolve(fileName + ".data");
        Path indexFile = dataDirectory.resolve(fileName + ".index");
        
        Map<byte[], Long> index = new HashMap<>();
        byte[] minKey = null;
        byte[] maxKey = null;
        long filePosition = 0;
        
        Map<byte[], byte[]> allEntries = new ConcurrentSkipListMap<>(Arrays::compare);
        
        for (SSTable sstable : sstables) {
            for (Map.Entry<byte[], byte[]> entry : sstable.getAllEntries()) {
                allEntries.put(entry.getKey(), entry.getValue());
            }
        }
        
        try (FileChannel dataChannel = FileChannel.open(dataFile, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileChannel indexChannel = FileChannel.open(indexFile, 
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            
            ByteBuffer dataBuffer = ByteBuffer.allocate(64 * 1024);
            ByteBuffer indexBuffer = ByteBuffer.allocate(32 * 1024);
            
            for (Map.Entry<byte[], byte[]> entry : allEntries.entrySet()) {
                byte[] key = entry.getKey();
                byte[] value = entry.getValue();
                
                if (Arrays.equals(value, TOMBSTONE)) {
                    continue;
                }
                
                if (minKey == null || Arrays.compare(key, minKey) < 0) {
                    minKey = key;
                }
                if (maxKey == null || Arrays.compare(key, maxKey) > 0) {
                    maxKey = key;
                }
                
                int keyLength = key.length;
                int valueLength = value.length;
                int totalLength = 4 + keyLength + 4 + valueLength;
                
                if (dataBuffer.remaining() < totalLength) {
                    dataBuffer.flip();
                    dataChannel.write(dataBuffer);
                    dataBuffer.clear();
                }
                
                dataBuffer.putInt(keyLength);
                dataBuffer.put(key);
                dataBuffer.putInt(valueLength);
                dataBuffer.put(value);
                
                index.put(key, filePosition);
                filePosition += totalLength;
                
                if (indexBuffer.remaining() < 4 + keyLength + 8) {
                    indexBuffer.flip();
                    indexChannel.write(indexBuffer);
                    indexBuffer.clear();
                }
                
                indexBuffer.putInt(keyLength);
                indexBuffer.put(key);
                indexBuffer.putLong(filePosition - totalLength);
            }
            
            dataBuffer.flip();
            dataChannel.write(dataBuffer);
            indexBuffer.flip();
            indexChannel.write(indexBuffer);
        }
        
        return new SSTable(dataFile, indexFile, level, filePosition, minKey, maxKey, index);
    }
    
    public Optional<byte[]> get(byte[] key) throws IOException {
        Long position = index.get(key);
        if (position == null) {
            return Optional.empty();
        }
        
        try (FileChannel dataChannel = FileChannel.open(dataFile, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            dataChannel.read(buffer, position);
            buffer.flip();
            
            int keyLength = buffer.getInt();
            byte[] storedKey = new byte[keyLength];
            buffer.get(storedKey);
            
            if (!Arrays.equals(key, storedKey)) {
                return Optional.empty();
            }
            
            int valueLength = buffer.getInt();
            byte[] value = new byte[valueLength];
            buffer.get(value);
            
            if (Arrays.equals(value, TOMBSTONE)) {
                return Optional.empty();
            }
            
            return Optional.of(value);
        }
    }
    
    public boolean contains(byte[] key) {
        return index.containsKey(key);
    }
    
    public Map<byte[], byte[]> getAllEntries() throws IOException {
        Map<byte[], byte[]> entries = new HashMap<>();
        
        try (FileChannel dataChannel = FileChannel.open(dataFile, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
            long position = 0;
            
            while (position < dataChannel.size()) {
                buffer.clear();
                int bytesRead = dataChannel.read(buffer, position);
                if (bytesRead <= 0) break;
                
                buffer.flip();
                while (buffer.hasRemaining()) {
                    int keyLength = buffer.getInt();
                    byte[] key = new byte[keyLength];
                    buffer.get(key);
                    
                    int valueLength = buffer.getInt();
                    byte[] value = new byte[valueLength];
                    buffer.get(value);
                    
                    entries.put(key, value);
                    position += 4 + keyLength + 4 + valueLength;
                }
            }
        }
        
        return entries;
    }
    
    public KeyRange getKeyRange() {
        return new KeyRange(minKey, maxKey);
    }
    
    public void delete() throws IOException {
        Files.deleteIfExists(dataFile);
        Files.deleteIfExists(indexFile);
    }
    
    public Path getDataFile() { return dataFile; }
    public Path getIndexFile() { return indexFile; }
    public int getLevel() { return level; }
    public long size() { return size; }
    public byte[] getMinKey() { return minKey; }
    public byte[] getMaxKey() { return maxKey; }
    
    public static class KeyRange {
        private final byte[] minKey;
        private final byte[] maxKey;
        
        public KeyRange(byte[] minKey, byte[] maxKey) {
            this.minKey = minKey;
            this.maxKey = maxKey;
        }
        
        public byte[] getMinKey() { return minKey; }
        public byte[] getMaxKey() { return maxKey; }
        
        public boolean contains(byte[] key) {
            if (minKey != null && Arrays.compare(key, minKey) < 0) {
                return false;
            }
            if (maxKey != null && Arrays.compare(key, maxKey) > 0) {
                return false;
            }
            return true;
        }
        
        public boolean overlaps(KeyRange other) {
            if (this.minKey == null || other.maxKey == null) return true;
            if (this.maxKey == null || other.minKey == null) return true;
            
            return Arrays.compare(this.minKey, other.maxKey) <= 0 && 
                   Arrays.compare(this.maxKey, other.minKey) >= 0;
        }
    }
}
