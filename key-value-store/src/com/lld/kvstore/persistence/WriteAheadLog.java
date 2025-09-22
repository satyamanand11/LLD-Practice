package com.lld.kvstore.persistence;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WriteAheadLog {
    private final Path logFile;
    private final FileChannel channel;
    private final ReentrantReadWriteLock lock;
    private final ByteBuffer buffer;
    private final int bufferSize;
    
    public WriteAheadLog() {
        this("wal.log");
    }
    
    public WriteAheadLog(String fileName) {
        try {
            this.logFile = Paths.get(fileName);
            this.channel = FileChannel.open(logFile, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.APPEND);
            this.lock = new ReentrantReadWriteLock();
            this.bufferSize = 64 * 1024;
            this.buffer = ByteBuffer.allocateDirect(bufferSize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize WAL", e);
        }
    }
    
    public void append(byte[] key, byte[] value, OperationType type) throws IOException {
        lock.writeLock().lock();
        try {
            LogEntry entry = new LogEntry(type, key, value, System.currentTimeMillis());
            writeEntry(entry);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void appendBatch(List<ByteKeyValueStore.KeyValuePair> pairs, OperationType type) throws IOException {
        lock.writeLock().lock();
        try {
            for (ByteKeyValueStore.KeyValuePair pair : pairs) {
                LogEntry entry = new LogEntry(type, pair.getKey(), pair.getValue(), System.currentTimeMillis());
                writeEntry(entry);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void writeEntry(LogEntry entry) throws IOException {
        byte[] serialized = entry.serialize();
        
        if (buffer.remaining() < serialized.length) {
            flush();
        }
        
        buffer.put(serialized);
        
        if (buffer.remaining() < 1024) {
            flush();
        }
    }
    
    public void flush() throws IOException {
        if (buffer.position() > 0) {
            buffer.flip();
            channel.write(buffer);
            channel.force(true);
            buffer.clear();
        }
    }
    
    public void sync() throws IOException {
        flush();
        channel.force(true);
    }
    
    public List<LogEntry> replay() throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        
        lock.readLock().lock();
        try {
            channel.close();
            FileChannel readChannel = FileChannel.open(logFile, StandardOpenOption.READ);
            
            ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
            long position = 0;
            
            while (position < readChannel.size()) {
                readBuffer.clear();
                int bytesRead = readChannel.read(readBuffer, position);
                if (bytesRead <= 0) break;
                
                readBuffer.flip();
                while (readBuffer.hasRemaining()) {
                    try {
                        LogEntry entry = LogEntry.deserialize(readBuffer);
                        entries.add(entry);
                        position += entry.getSerializedSize();
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            
            readChannel.close();
            
            this.channel = FileChannel.open(logFile, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.APPEND);
                
        } finally {
            lock.readLock().unlock();
        }
        
        return entries;
    }
    
    public void truncate(long position) throws IOException {
        lock.writeLock().lock();
        try {
            flush();
            channel.truncate(position);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void clear() throws IOException {
        lock.writeLock().lock();
        try {
            channel.close();
            Files.deleteIfExists(logFile);
            this.channel = FileChannel.open(logFile, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.APPEND);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            flush();
            channel.close();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public enum OperationType {
        PUT, DELETE
    }
    
    public static class LogEntry {
        private final OperationType type;
        private final byte[] key;
        private final byte[] value;
        private final long timestamp;
        
        public LogEntry(OperationType type, byte[] key, byte[] value, long timestamp) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.timestamp = timestamp;
        }
        
        public byte[] serialize() {
            int keyLength = key != null ? key.length : 0;
            int valueLength = value != null ? value.length : 0;
            int totalSize = 1 + 8 + 4 + keyLength + 4 + valueLength;
            
            ByteBuffer buffer = ByteBuffer.allocate(totalSize);
            buffer.put((byte) type.ordinal());
            buffer.putLong(timestamp);
            buffer.putInt(keyLength);
            if (key != null) buffer.put(key);
            buffer.putInt(valueLength);
            if (value != null) buffer.put(value);
            
            return buffer.array();
        }
        
        public static LogEntry deserialize(ByteBuffer buffer) {
            OperationType type = OperationType.values()[buffer.get()];
            long timestamp = buffer.getLong();
            int keyLength = buffer.getInt();
            byte[] key = new byte[keyLength];
            buffer.get(key);
            int valueLength = buffer.getInt();
            byte[] value = new byte[valueLength];
            buffer.get(value);
            
            return new LogEntry(type, key, value, timestamp);
        }
        
        public int getSerializedSize() {
            int keyLength = key != null ? key.length : 0;
            int valueLength = value != null ? value.length : 0;
            return 1 + 8 + 4 + keyLength + 4 + valueLength;
        }
        
        public OperationType getType() { return type; }
        public byte[] getKey() { return key; }
        public byte[] getValue() { return value; }
        public long getTimestamp() { return timestamp; }
    }
}
