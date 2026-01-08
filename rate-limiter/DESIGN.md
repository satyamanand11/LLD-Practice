# Rate Limiter System - Design Document

## Overview
A configurable rate limiter system that supports multiple rate limiting algorithms (TokenBucket, SlidingWindowLog, etc.) and enforces limits per client per endpoint.

---

## Requirements Analysis

### Functional Requirements

1. **Configuration loaded at startup (once)**
   - Load endpoint configurations from a source (file, database, etc.)
   - Configuration specifies algorithm type and parameters
   - Immutable after startup (or reload mechanism)

2. **System receives requests with (clientId, endpoint)**
   - Each request identifies a client and target endpoint
   - Need to track rate limit state per client per endpoint

3. **Each endpoint has configuration:**
   - Algorithm type (TokenBucket, SlidingWindowLog, FixedWindow, etc.)
   - Algorithm-specific parameters:
     - TokenBucket: capacity, refillRatePerSecond
     - SlidingWindowLog: windowSizeMs, maxRequests
     - FixedWindow: windowSizeMs, maxRequests

4. **Enforce rate limits by checking clientId against endpoint's configuration**
   - Track state per (clientId, endpoint) pair
   - Apply algorithm-specific logic
   - Thread-safe concurrent access

5. **Return structured result:**
   - `allowed: boolean` - Whether request is allowed
   - `remaining: int` - Remaining requests/tokens
   - `retryAfterMs: long | null` - Milliseconds to wait before retry (null if allowed)

6. **Default limit if endpoint not configured**
   - Fallback configuration
   - Default algorithm (e.g., TokenBucket with default params)

---

## Design Patterns Analysis

### 1. **Strategy Pattern** ✅ **ESSENTIAL**

**Use Case**: Multiple rate limiting algorithms (TokenBucket, SlidingWindowLog, FixedWindow)

**Why it makes sense:**
- Different algorithms have different logic
- Easy to add new algorithms without changing core code
- Follows Open/Closed Principle
- Each algorithm is a clear "strategy" for rate limiting

**Implementation:**
- Strategy Interface: `RateLimitingAlgorithm`
- Concrete Strategies: `TokenBucketAlgorithm`, `SlidingWindowLogAlgorithm`, `FixedWindowAlgorithm`
- Context: `RateLimiterService` uses strategy

**Verdict**: ✅ **Perfect fit - Core pattern for this system**

---

### 2. **Factory Pattern** ✅ **EXCELLENT FIT**

**Use Case**: Creating algorithm instances from configuration

**Why it makes sense:**
- Configuration specifies algorithm type as string
- Need to instantiate correct algorithm class
- Centralizes creation logic
- Easy to add new algorithms

**Implementation:**
- Factory: `RateLimitingAlgorithmFactory`
- Creates: `TokenBucketAlgorithm`, `SlidingWindowLogAlgorithm`, etc.
- Input: Algorithm type string + parameters

**Verdict**: ✅ **Perfect fit - Algorithm creation**

---

### 3. **Repository Pattern** ✅ **GOOD FIT**

**Use Case**: Storing rate limit state per (clientId, endpoint)

**Why it makes sense:**
- Need to track state across requests
- Abstract storage implementation
- Easy to swap in-memory → Redis/Database
- Testability

**Implementation:**
- Interface: `RateLimitStateRepository`
- Implementation: `InMemoryRateLimitStateRepository`
- Key: Composite key (clientId, endpoint)

**Verdict**: ✅ **Good fit - State storage**

---

### 4. **Singleton Pattern** ⚠️ **CONSIDER**

**Use Case**: Configuration manager, Rate limiter service

**Why it might make sense:**
- Configuration loaded once at startup
- Single rate limiter instance

**Better Alternative:**
- Use dependency injection
- Configuration as immutable object
- Service as singleton at application level (not enforced by pattern)

**Verdict**: ⚠️ **Use service pattern, singleton at DI level**

---

### 5. **Builder Pattern** ✅ **GOOD FIT**

**Use Case**: Building endpoint configurations

**Why it makes sense:**
- Configuration has many optional parameters
- Different algorithms need different parameters
- Clearer than huge constructor

**Implementation:**
- `EndpointConfigBuilder`
- Fluent API for configuration

**Verdict**: ✅ **Good fit - Configuration building**

---

## Domain Model Design

### Core Entities

#### 1. **RateLimitResult**
```java
public class RateLimitResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs;  // null if allowed
    
    // Constructor, getters
}
```

#### 2. **EndpointConfig**
```java
public class EndpointConfig {
    private final String endpoint;
    private final String algorithmType;  // "TokenBucket", "SlidingWindowLog", etc.
    private final Map<String, Object> parameters;  // Algorithm-specific params
    
    // Constructor, getters
}
```

#### 3. **RateLimitState**
```java
// Algorithm-specific state
// TokenBucket: tokens, lastRefillTime
// SlidingWindowLog: List<Long> requestTimestamps
// FixedWindow: count, windowStartTime

// Stored per (clientId, endpoint) pair
```

#### 4. **AlgorithmState** (Interface)
```java
// Different implementations per algorithm
// TokenBucketState, SlidingWindowLogState, FixedWindowState
```

---

## Algorithm Design

### Strategy Interface

```java
public interface RateLimitingAlgorithm {
    /**
     * Checks if request is allowed and updates state.
     * 
     * @param state Current algorithm state (may be null for first request)
     * @param config Endpoint configuration
     * @return RateLimitResult
     */
    RateLimitResult checkLimit(AlgorithmState state, EndpointConfig config);
    
    /**
     * Creates initial state for a new (clientId, endpoint) pair.
     */
    AlgorithmState createInitialState();
    
    /**
     * Gets algorithm name.
     */
    String getAlgorithmName();
}
```

### Algorithm Implementations

#### 1. **TokenBucketAlgorithm**

**Parameters:**
- `capacity`: int - Maximum tokens
- `refillRatePerSecond`: double - Tokens added per second

**State:**
- `tokens`: double - Current token count
- `lastRefillTime`: long - Last refill timestamp

**Logic:**
1. Refill tokens based on time elapsed
2. If tokens >= 1, allow request and decrement
3. Otherwise, reject and calculate retryAfterMs

#### 2. **SlidingWindowLogAlgorithm**

**Parameters:**
- `windowSizeMs`: long - Time window in milliseconds
- `maxRequests`: int - Maximum requests in window

**State:**
- `requestTimestamps`: List<Long> - Timestamps of requests in window

**Logic:**
1. Remove timestamps outside window
2. If count < maxRequests, allow and add timestamp
3. Otherwise, reject and calculate retryAfterMs

#### 3. **FixedWindowAlgorithm**

**Parameters:**
- `windowSizeMs`: long - Fixed window size
- `maxRequests`: int - Maximum requests per window

**State:**
- `count`: int - Request count in current window
- `windowStartTime`: long - Start of current window

**Logic:**
1. If current time outside window, reset window
2. If count < maxRequests, allow and increment
3. Otherwise, reject and calculate retryAfterMs

---

## Service Layer Design

### 1. **RateLimiterService** (Core Service)

**Responsibilities:**
- Load configuration at startup
- Route requests to appropriate algorithm
- Manage state per (clientId, endpoint)
- Return structured results

**Key Methods:**
```java
- RateLimitResult checkLimit(String clientId, String endpoint)
- void loadConfiguration(Map<String, EndpointConfig> configs)
- EndpointConfig getEndpointConfig(String endpoint)
```

**Thread-Safety:**
- State repository uses ConcurrentHashMap
- Algorithm operations are atomic
- Per (clientId, endpoint) locking if needed

### 2. **ConfigurationService**

**Responsibilities:**
- Load configuration from source
- Provide default configuration
- Validate configuration

**Key Methods:**
```java
- Map<String, EndpointConfig> loadConfiguration()
- EndpointConfig getDefaultConfig()
```

---

## Architecture Overview

```
┌─────────────────────────────────────┐
│   RateLimiterFacade (Facade)       │
└─────────────────────────────────────┘
              │
    ┌─────────▼─────────┐
    │ RateLimiterService│
    └─────────┬─────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
┌───▼───┐ ┌──▼───┐ ┌───▼───┐
│Config │ │State │ │Factory│
│Service│ │Repo  │ │       │
└───┬───┘ └──┬───┘ └───┬───┘
    │        │         │
    │    ┌───▼─────────▼───┐
    │    │  Algorithm       │
    │    │  (Strategy)       │
    │    └──────────────────┘
    │
┌───▼───────────────────────┐
│  EndpointConfig           │
│  (Immutable)              │
└───────────────────────────┘
```

---

## Concurrency & Thread-Safety

### State Management

**Approach**: Per (clientId, endpoint) state isolation

**Options:**
1. **ConcurrentHashMap** - Thread-safe map
2. **Per-key locking** - Fine-grained locks
3. **Atomic operations** - Algorithm-specific atomicity

**Recommendation:**
- Use ConcurrentHashMap for state storage
- Algorithms handle their own atomicity
- For complex algorithms, use per-key locking

### Algorithm Thread-Safety

**TokenBucket:**
- Atomic refill calculation
- Atomic token decrement
- Use synchronized or AtomicDouble

**SlidingWindowLog:**
- Synchronized list operations
- Or use ConcurrentLinkedQueue

**FixedWindow:**
- Atomic window reset
- Atomic count increment

---

## Configuration Design

### Configuration Structure

```json
{
  "endpoints": {
    "/api/users": {
      "algorithm": "TokenBucket",
      "parameters": {
        "capacity": 100,
        "refillRatePerSecond": 10.0
      }
    },
    "/api/orders": {
      "algorithm": "SlidingWindowLog",
      "parameters": {
        "windowSizeMs": 60000,
        "maxRequests": 50
      }
    }
  },
  "default": {
    "algorithm": "TokenBucket",
    "parameters": {
      "capacity": 50,
      "refillRatePerSecond": 5.0
    }
  }
}
```

### Configuration Loading

**Options:**
1. JSON file
2. Properties file
3. Database
4. Environment variables
5. In-memory (for testing)

**Implementation:**
- ConfigurationService loads at startup
- Immutable after loading (or reload mechanism)
- Default config for unconfigured endpoints

---

## Key Design Decisions

### 1. **State Storage Per (clientId, endpoint)**

**Decision**: Store state separately for each (clientId, endpoint) pair

**Rationale:**
- Each client has independent rate limits
- Each endpoint has independent limits
- Clear isolation

**Storage Key:**
- Composite key: `clientId:endpoint` (string)
- Or: `ClientEndpointKey` value object (better)

### 2. **Algorithm State as Interface**

**Decision**: `AlgorithmState` interface with algorithm-specific implementations

**Rationale:**
- Different algorithms need different state
- Type-safe state handling
- Extensible

### 3. **Configuration Immutability**

**Decision**: Configuration loaded once, immutable after

**Rationale:**
- Thread-safe
- No need for synchronization
- Simple reload mechanism if needed

### 4. **Default Configuration**

**Decision**: Fallback to default config for unconfigured endpoints

**Rationale:**
- Graceful degradation
- System works even if config incomplete
- Configurable default

---

## Error Handling

### Exceptions

1. **InvalidConfigurationException**
   - Invalid algorithm type
   - Missing required parameters
   - Invalid parameter values

2. **AlgorithmNotFoundException**
   - Algorithm type not supported

3. **ConfigurationLoadException**
   - Failed to load configuration

---

## Extensibility

### Adding New Algorithms

1. Implement `RateLimitingAlgorithm` interface
2. Implement `AlgorithmState` interface
3. Register in `RateLimitingAlgorithmFactory`
4. Update configuration schema

### Adding New Configuration Sources

1. Implement `ConfigurationLoader` interface
2. Update `ConfigurationService`

---

## Testing Considerations

### Unit Tests
- Each algorithm independently
- Configuration loading
- Factory creation

### Integration Tests
- End-to-end rate limiting
- Concurrent requests
- State persistence

### Performance Tests
- High throughput
- Memory usage
- Lock contention

---

## Implementation Checklist

- [ ] Domain Models (RateLimitResult, EndpointConfig, AlgorithmState)
- [ ] Algorithm Interface and Implementations
- [ ] Algorithm Factory
- [ ] State Repository
- [ ] Configuration Service
- [ ] Rate Limiter Service
- [ ] Facade
- [ ] Main.java demo

---

## Summary

This design provides:
- ✅ Multiple rate limiting algorithms (Strategy pattern)
- ✅ Configurable per endpoint
- ✅ Thread-safe concurrent access
- ✅ Structured result format
- ✅ Default configuration fallback
- ✅ Extensible architecture

**Ready for implementation!** 🚀

