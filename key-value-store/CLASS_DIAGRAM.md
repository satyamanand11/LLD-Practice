# Key-Value Store Class Diagram

## Overview
This design implements a thread-safe Key-Value data store with type safety, following SOLID principles and design patterns.

## Key Design Patterns Used:
1. **Command Pattern** - For operations (similar to Redis)
2. **Strategy Pattern** - For different value type handling
3. **Factory Pattern** - For creating value instances
4. **Observer Pattern** - For monitoring store operations
5. **Template Method Pattern** - For common operation structure

## Concurrency Strategy:
- **Read-Write Locks** - For fine-grained concurrency control
- **Atomic Operations** - For primitive operations
- **Thread-Safe Collections** - For internal data structures
- **Immutable Value Objects** - Where possible

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                KeyValueStore                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - store: ConcurrentHashMap<String, ValueWrapper>                               │
│ - typeRegistry: ConcurrentHashMap<String, ValueType>                           │
│ - readWriteLock: ReentrantReadWriteLock                                        │
│ - observers: List<StoreObserver>                                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + put(key: String, value: Object): Result<V>                                   │
│ + get(key: String): Result<V>                                                  │
│ + delete(key: String): Result<Boolean>                                         │
│ + executeCommand(command: Command): Result<T>                                  │
│ + addObserver(observer: StoreObserver): void                                   │
│ + removeObserver(observer: StoreObserver): void                                │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                ValueWrapper                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - value: Value                                                                  │
│ - type: ValueType                                                              │
│ - createdAt: Instant                                                           │
│ - updatedAt: Instant                                                           │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + getValue(): Value                                                            │
│ + getType(): ValueType                                                         │
│ + updateValue(value: Value): void                                              │
│ + getCreatedAt(): Instant                                                      │
│ + getUpdatedAt(): Instant                                                      │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                  Value                                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + getType(): ValueType                                                         │
│ + getValue(): Object                                                           │
│ + equals(obj: Object): boolean                                                 │
│ + hashCode(): int                                                              │
│ + toString(): String                                                           │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        ▲
                                        │
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│      PrimitiveValue     │ │     CollectionValue     │ │      NullValue          │
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ - value: Object         │ │ - values: Collection    │ ├─────────────────────────┤
│ - type: ValueType       │ │ - type: ValueType       │ │ - type: ValueType.NULL  │
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ + getValue(): Object    │ │ + getValues(): Collection│ │ + getValue(): null     │
│ + getType(): ValueType  │ │ + getType(): ValueType  │ │ + getType(): ValueType │
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘
                                        │
                                        │
                                        ▼
                    ┌─────────────────────────────────────────┐
                    │            CollectionValue              │
                    │              (Abstract)                 │
                    ├─────────────────────────────────────────┤
                    │ - values: Collection<Object>           │
                    │ - elementType: ValueType               │
                    ├─────────────────────────────────────────┤
                    │ + addValue(value: Object): boolean     │
                    │ + removeValue(value: Object): boolean  │
                    │ + containsValue(value: Object): boolean│
                    │ + getSize(): int                       │
                    └─────────────────────────────────────────┘
                                        ▲
                                        │
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│       ListValue         │ │        SetValue          │ │       MapValue          │
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ - values: List<Object>  │ │ - values: Set<Object>   │ │ - values: Map<String,   │
│ - elementType: ValueType│ │ - elementType: ValueType│ │          Object>        │
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ + addValue(value: Object│ │ + addValue(value: Object│ │ + putValue(key: String, │
│   ): boolean            │ │   ): boolean            │ │   value: Object): void  │
│ + removeValue(value:    │ │ + removeValue(value:    │ │ + getValue(key: String):│
│   Object): boolean      │ │   Object): boolean      │ │   Object                │
│ + getValue(index: int): │ │ + containsValue(value:  │ │ + removeValue(key:      │
│   Object                │ │   Object): boolean      │ │   String): Object       │
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                                ValueType                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│ STRING, INTEGER, LONG, DOUBLE, FLOAT, BOOLEAN,                                 │
│ LIST, SET, MAP, NULL                                                           │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + isPrimitive(): boolean                                                       │
│ + isCollection(): boolean                                                      │
│ + getElementType(): ValueType                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                                 Command                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + execute(store: KeyValueStore): Result<T>                                     │
│ + getCommandType(): CommandType                                                │
│ + getKey(): String                                                             │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        ▲
                                        │
                                        │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
┌─────────────────────────┐ ┌─────────────────────────┐ ┌─────────────────────────┐
│       PutCommand        │ │       GetCommand        │ │     DeleteCommand       │
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ - key: String           │ │ - key: String           │ │ - key: String           │
│ - value: Object         │ │ - index: Optional<Integer│ │ - value: Optional<Object│
├─────────────────────────┤ ├─────────────────────────┤ ├─────────────────────────┤
│ + execute(store:        │ │ + execute(store:        │ │ + execute(store:        │
│   KeyValueStore):       │ │   KeyValueStore):       │ │   KeyValueStore):       │
│   Result<Boolean>       │ │   Result<Value>         │ │   Result<Boolean>       │
└─────────────────────────┘ └─────────────────────────┘ └─────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                                 Result<T>                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - success: boolean                                                              │
│ - data: T                                                                       │
│ - error: Optional<String>                                                       │
│ - timestamp: Instant                                                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + isSuccess(): boolean                                                         │
│ + getData(): T                                                                 │
│ + getError(): Optional<String>                                                 │
│ + getTimestamp(): Instant                                                      │
│ + ofSuccess(data: T): Result<T>                                                │
│ + ofError(error: String): Result<T>                                            │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              StoreObserver                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + onPut(key: String, value: Value): void                                       │
│ + onGet(key: String, value: Value): void                                       │
│ + onDelete(key: String): void                                                  │
│ + onError(operation: String, error: String): void                              │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            ValueFactory                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + createPrimitiveValue(value: Object): PrimitiveValue                          │
│ + createListValue(values: List<Object>): ListValue                             │
│ + createSetValue(values: Set<Object>): SetValue                                │
│ + createMapValue(values: Map<String, Object>): MapValue                        │
│ + createNullValue(): NullValue                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            TypeValidator                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + validateType(value: Object, expectedType: ValueType): boolean                │
│ + validateCollectionType(values: Collection<Object>,                           │
│   expectedElementType: ValueType): boolean                                     │
│ + getValueType(value: Object): ValueType                                       │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            NetworkService                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - serverSocket: ServerSocket                                                   │
│ - threadPool: ExecutorService                                                  │
│ - connectionPool: ConnectionPool                                               │
│ - requestHandler: RequestHandler                                               │
│ - metrics: MetricsCollector                                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + start(port: int): void                                                       │
│ + stop(): void                                                                  │
│ + handleRequest(request: Request): Response                                    │
│ + getMetrics(): Metrics                                                        │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            RequestHandler                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - kvStore: KeyValueStore                                                       │
│ - serializer: Serializer                                                       │
│ - batchProcessor: BatchProcessor                                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + handleGet(key: byte[]): Response                                             │
│ + handlePut(key: byte[], value: byte[]): Response                             │
│ + handleDelete(key: byte[]): Response                                          │
│ + handleBatch(requests: List<Request>): Response                              │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            PersistenceLayer                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - wal: WriteAheadLog                                                           │
│ - memtable: MemTable                                                           │
│ - sstables: List<SSTable>                                                      │
│ - bloomFilter: BloomFilter                                                     │
│ - compactionManager: CompactionManager                                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + write(key: byte[], value: byte[]): void                                      │
│ + read(key: byte[]): Optional<byte[]>                                          │
│ + delete(key: byte[]): void                                                    │
│ + flush(): void                                                                │
│ + compact(): void                                                              │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            WriteAheadLog                                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - logFile: RandomAccessFile                                                    │
│ - buffer: ByteBuffer                                                           │
│ - syncMode: SyncMode                                                           │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + append(entry: LogEntry): void                                                │
│ + sync(): void                                                                 │
│ + replay(): List<LogEntry>                                                     │
│ + truncate(position: long): void                                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            MemTable                                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - data: ConcurrentSkipListMap<byte[], byte[]>                                  │
│ - size: AtomicLong                                                             │
│ - maxSize: long                                                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + put(key: byte[], value: byte[]): void                                        │
│ + get(key: byte[]): Optional<byte[]>                                           │
│ + delete(key: byte[]): void                                                    │
│ + isFull(): boolean                                                            │
│ + flush(): SSTable                                                             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            SSTable                                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - dataFile: File                                                               │
│ - indexFile: File                                                              │
│ - bloomFilter: BloomFilter                                                     │
│ - level: int                                                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + get(key: byte[]): Optional<byte[]>                                           │
│ + contains(key: byte[]): boolean                                               │
│ + getKeyRange(): KeyRange                                                      │
│ + merge(other: SSTable): SSTable                                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            ReplicationManager                                  │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - master: boolean                                                              │
│ - replicas: List<ReplicaNode>                                                  │
│ - replicationLog: ReplicationLog                                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + replicate(operation: Operation): void                                        │
│ + syncWithMaster(): void                                                       │
│ + promoteToMaster(): void                                                      │
│ + addReplica(node: ReplicaNode): void                                          │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            MetricsCollector                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - operationCounters: Map<String, AtomicLong>                                   │
│ - latencyHistogram: Histogram                                                  │
│ - throughputMeter: Meter                                                       │
│ - memoryUsage: MemoryUsage                                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + recordOperation(operation: String, latency: long): void                      │
│ + recordThroughput(operations: long): void                                     │
│ + getMetrics(): Metrics                                                        │
│ + reset(): void                                                                │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                            BatchProcessor                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│ - batchSize: int                                                               │
│ - flushInterval: Duration                                                      │
│ - pendingBatches: Queue<Batch>                                                 │
│ - executor: ScheduledExecutorService                                           │
├─────────────────────────────────────────────────────────────────────────────────┤
│ + addOperation(operation: Operation): void                                     │
│ + flush(): void                                                                │
│ + processBatch(batch: Batch): void                                             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## High-Performance Features:

### Concurrency & Performance:
1. **Thread-Safe Operations**: All operations use appropriate locking mechanisms
2. **Read-Write Locks**: Multiple readers, single writer for optimal performance
3. **Atomic Updates**: Critical sections are protected with proper synchronization
4. **Immutable Value Objects**: Where possible, values are immutable to prevent race conditions
5. **Thread-Safe Collections**: Internal data structures are thread-safe
6. **Lock-Free Reads**: Read operations are optimized for minimal contention
7. **Byte[] Operations**: Native byte array support for maximum performance
8. **Write Batching**: Batch writes for high throughput
9. **Async Operations**: Non-blocking I/O for network operations
10. **Memory-Mapped Files**: Fast persistence layer

### Network Service:
1. **TCP Server**: High-performance TCP server with NIO
2. **Connection Pooling**: Efficient connection management
3. **Request Batching**: Batch multiple operations in single request
4. **Protocol Buffers**: Efficient serialization
5. **Load Balancing**: Distribute load across multiple instances

### Fault Tolerance:
1. **Write-Ahead Logging (WAL)**: Ensure durability
2. **Replication**: Master-slave replication for fault tolerance
3. **Checkpointing**: Periodic snapshots for recovery
4. **Health Monitoring**: Continuous health checks
5. **Graceful Degradation**: Continue operation during partial failures

### Persistence:
1. **LSM-Tree**: Log-Structured Merge-Tree for high write throughput
2. **SSTable**: Sorted String Tables for efficient reads
3. **Compaction**: Background compaction for space efficiency
4. **Bloom Filters**: Fast key existence checks
5. **Index Files**: Fast key-to-value mapping

## SOLID Principles Applied:

1. **Single Responsibility**: Each class has a single, well-defined responsibility
2. **Open/Closed**: Easy to extend with new value types and commands
3. **Liskov Substitution**: All value types can be substituted for their base types
4. **Interface Segregation**: Small, focused interfaces for different concerns
5. **Dependency Inversion**: High-level modules depend on abstractions, not concretions
