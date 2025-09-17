# High-Performance Key-Value Store

A production-ready, thread-safe Key-Value store implementation designed for high throughput, low latency, and fault tolerance. Built with Java following SOLID principles and design patterns.

## Problem Statement

Design and implement a high throughput, low latency, fault-tolerant persistent key-value store that:

- **Runs as a network service** supporting many clients in a data center
- **Uses standard key-value operations**: get, put, delete
- **Handles byte[] keys and values** for maximum performance
- **Optimizes for high write throughput** on a single node
- **Ensures fault tolerance** and data persistence
- **Supports concurrent access** from multiple clients
- **Provides type safety** for different value types (primitives and collections)

## Key Features

### 🚀 **High Performance & Concurrency**
- Thread-safe operations with ReadWrite locks
- Byte[] operations for maximum performance
- Async operations with CompletableFuture
- Batch operations for high throughput
- Connection pooling and NIO networking

### 🛡️ **Fault Tolerance & Persistence**
- Write-Ahead Logging (WAL) for durability
- LSM-Tree architecture with MemTable and SSTables
- Master-slave replication for fault tolerance
- Automatic compaction for space efficiency

### 🏗️ **Design Patterns & SOLID Principles**
- **Command Pattern** for operations (Redis-like)
- **Strategy Pattern** for value types
- **Observer Pattern** for monitoring
- **Factory Pattern** for value creation
- **Single Responsibility** - each class has one purpose
- **Open/Closed** - easy to extend with new types
- **Interface Segregation** - focused interfaces

### 🌐 **Network Service**
- TCP server with NIO for high throughput
- Request/Response serialization
- Connection management
- Performance metrics collection

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    High-Performance Key-Value Store            │
├─────────────────────────────────────────────────────────────────┤
│  Network Layer    │  Service Layer    │  Persistence Layer     │
│  - TCP Server     │  - Command Exec   │  - WAL                 │
│  - NIO I/O        │  - Type Safety    │  - MemTable            │
│  - Serialization  │  - Concurrency    │  - SSTables            │
│  - Metrics        │  - Batching       │  - Compaction          │
└─────────────────────────────────────────────────────────────────┘
```

## Value Types Supported

### Primitive Types
- String, Integer, Long, Double, Float, Boolean
- Null values

### Collection Types
- **List**: Ordered collection of primitives
- **Set**: Unordered unique collection of primitives  
- **Map**: Key-value pairs with string keys

## Usage Examples

### Basic Operations
```java
HighPerformanceKeyValueStore store = new HighPerformanceKeyValueStore(1000, 100, 1000);

// Basic operations
byte[] key = "user:1".getBytes();
byte[] value = "John Doe".getBytes();

Result<Boolean> putResult = store.put(key, value);
Result<byte[]> getResult = store.get(key);
Result<Boolean> deleteResult = store.delete(key);
```

### Batch Operations
```java
List<KeyValuePair> pairs = Arrays.asList(
    new KeyValuePair("key1".getBytes(), "value1".getBytes()),
    new KeyValuePair("key2".getBytes(), "value2".getBytes())
);

Result<Boolean> batchResult = store.putBatch(pairs);
```

### Async Operations
```java
CompletableFuture<Result<Boolean>> putFuture = store.putAsync(key, value);
CompletableFuture<Result<byte[]>> getFuture = store.getAsync(key);
```

### Network Service
```java
// Start network service on port 8080
Result<Boolean> startResult = store.start(8080);

// Get performance metrics
Result<Metrics> metricsResult = store.getMetrics();
```

## Performance Characteristics

- **High Write Throughput**: Optimized for write-heavy workloads
- **Low Latency**: Sub-millisecond response times for in-memory operations
- **Concurrent Access**: Thread-safe operations with minimal contention
- **Memory Efficient**: LSM-Tree with automatic compaction
- **Fault Tolerant**: WAL + replication for data durability

## Design Patterns Used

1. **Command Pattern**: Encapsulates operations for undo/redo and queuing
2. **Strategy Pattern**: Different strategies for value type handling
3. **Observer Pattern**: Event notification for store operations
4. **Factory Pattern**: Creates appropriate value instances
5. **Template Method**: Common structure for operations

## SOLID Principles Applied

- **S**ingle Responsibility: Each class has one clear purpose
- **O**pen/Closed: Easy to extend with new value types
- **L**iskov Substitution: All implementations are substitutable
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Depend on abstractions, not concretions

## Concurrency Features

- **ReadWrite Locks**: Multiple readers, single writer
- **Atomic Operations**: Thread-safe primitive operations
- **Thread-Safe Collections**: ConcurrentSkipListMap for ordered data
- **Lock-Free Reads**: Optimized read operations
- **Async Processing**: Non-blocking I/O operations

## File Structure

```
src/
├── Main.java                                    # Demo application
├── com/lld/kvstore/
│   ├── enums/                                   # Value and Command types
│   ├── interfaces/                              # Core interfaces
│   ├── models/                                  # Value implementations
│   ├── services/                                # Main store implementation
│   ├── network/                                 # Network service layer
│   ├── persistence/                             # Storage layer
│   ├── replication/                             # Fault tolerance
│   ├── metrics/                                 # Performance monitoring
│   ├── observers/                               # Event handling
│   └── commands/                                # Command pattern
```

## Running the Demo

```bash
# Compile the project
javac -d out src/**/*.java

# Run the demo
java -cp out Main
```

## Key Classes

- **`HighPerformanceKeyValueStore`**: Main store implementation
- **`ByteKeyValueStore`**: High-performance interface
- **`MemTable`**: In-memory storage with thread safety
- **`SSTable`**: Persistent storage with LSM-Tree
- **`WriteAheadLog`**: Durability guarantee
- **`NetworkService`**: TCP server with NIO
- **`ReplicationManager`**: Fault tolerance
- **`MetricsCollector`**: Performance monitoring

## Requirements Met

✅ **High throughput, low latency** - Optimized for performance  
✅ **Fault-tolerant persistent** - WAL + replication + LSM-Tree  
✅ **Network service** - TCP server supporting multiple clients  
✅ **Standard operations** - get, put, delete with byte[] support  
✅ **High write throughput** - Batch operations and async processing  
✅ **Concurrent access** - Thread-safe with proper locking  
✅ **Type safety** - Support for primitives and collections  
✅ **SOLID principles** - Clean, maintainable architecture  

## Future Enhancements

- Bloom filters for faster key lookups
- Compression for SSTables
- Load balancing across multiple nodes
- Advanced replication strategies
- Query language support
- Memory-mapped files for larger datasets
