# Rate Limiter System

A configurable rate limiter system that supports multiple rate limiting algorithms (TokenBucket, SlidingWindowLog, FixedWindow) and enforces limits per client per endpoint.

## Features

✅ **Configuration loaded at startup** (once)  
✅ **Multiple rate limiting algorithms** (TokenBucket, SlidingWindowLog, FixedWindow)  
✅ **Per-endpoint configuration** with algorithm-specific parameters  
✅ **Per-client rate limiting** (independent limits per client)  
✅ **Structured result format** (allowed, remaining, retryAfterMs)  
✅ **Default configuration fallback** for unconfigured endpoints  
✅ **Thread-safe** concurrent access  

## Architecture

### Design Patterns

1. **Strategy Pattern**: `RateLimitingAlgorithm` - Multiple algorithm implementations
2. **Factory Pattern**: `RateLimitingAlgorithmFactory` - Algorithm creation
3. **Repository Pattern**: `RateLimitStateRepository` - State storage abstraction
4. **Facade Pattern**: `RateLimiterFacade` - Unified system interface
5. **Singleton Pattern**: `RateLimiterFacadeImpl` - Single system instance

### Core Components

#### Domain Layer
- **RateLimitResult**: Structured result (allowed, remaining, retryAfterMs)
- **EndpointConfig**: Endpoint configuration with algorithm type and parameters
- **ClientEndpointKey**: Composite key (clientId, endpoint)

#### Algorithm Layer
- **RateLimitingAlgorithm**: Strategy interface
- **TokenBucketAlgorithm**: Token bucket implementation
- **SlidingWindowLogAlgorithm**: Sliding window log implementation
- **FixedWindowAlgorithm**: Fixed window implementation
- **AlgorithmState**: Interface for algorithm-specific state

#### Service Layer
- **RateLimiterService**: Core rate limiting logic
- **ConfigurationService**: Configuration management
- **RateLimitingAlgorithmFactory**: Algorithm factory

#### Repository Layer
- **RateLimitStateRepository**: State storage interface
- **InMemoryRateLimitStateRepository**: In-memory implementation

## Algorithms

### 1. TokenBucket

**Parameters:**
- `capacity`: int - Maximum tokens
- `refillRatePerSecond`: double - Tokens added per second

**Behavior:**
- Tokens refill continuously based on time elapsed
- Request consumes 1 token
- Allows burst up to capacity

### 2. SlidingWindowLog

**Parameters:**
- `windowSizeMs`: long - Time window in milliseconds
- `maxRequests`: int - Maximum requests in window

**Behavior:**
- Maintains log of request timestamps
- Removes timestamps outside window
- Allows requests if count < maxRequests

### 3. FixedWindow

**Parameters:**
- `windowSizeMs`: long - Fixed window size in milliseconds
- `maxRequests`: int - Maximum requests per window

**Behavior:**
- Fixed time windows (e.g., every 10 seconds)
- Resets count at window boundary
- Allows requests if count < maxRequests

## Usage Example

```java
// Get facade instance
RateLimiterFacade facade = RateLimiterFacadeImpl.getInstance();

// Load configuration
Map<String, EndpointConfig> configs = new HashMap<>();
Map<String, Object> params = new HashMap<>();
params.put("capacity", 100);
params.put("refillRatePerSecond", 10.0);
EndpointConfig config = new EndpointConfig("/api/users", "TokenBucket", params);
configs.put("/api/users", config);

Map<String, Object> defaultParams = new HashMap<>();
defaultParams.put("capacity", 50);
defaultParams.put("refillRatePerSecond", 5.0);
EndpointConfig defaultConfig = new EndpointConfig("default", "TokenBucket", defaultParams);

facade.loadConfiguration(configs, defaultConfig);

// Check rate limit
RateLimitResult result = facade.checkLimit("client1", "/api/users");
if (result.isAllowed()) {
    // Process request
    System.out.println("Remaining: " + result.getRemaining());
} else {
    // Rate limited
    System.out.println("Retry after: " + result.getRetryAfterMs() + "ms");
}
```

## Configuration Format

### Example Configuration

```java
// TokenBucket
Map<String, Object> tokenBucketParams = new HashMap<>();
tokenBucketParams.put("capacity", 10);
tokenBucketParams.put("refillRatePerSecond", 2.0);
EndpointConfig config = new EndpointConfig(
    "/api/users",
    "TokenBucket",
    tokenBucketParams
);

// SlidingWindowLog
Map<String, Object> slidingWindowParams = new HashMap<>();
slidingWindowParams.put("windowSizeMs", 60000L); // 60 seconds
slidingWindowParams.put("maxRequests", 5);
EndpointConfig config = new EndpointConfig(
    "/api/orders",
    "SlidingWindowLog",
    slidingWindowParams
);

// FixedWindow
Map<String, Object> fixedWindowParams = new HashMap<>();
fixedWindowParams.put("windowSizeMs", 10000L); // 10 seconds
fixedWindowParams.put("maxRequests", 3);
EndpointConfig config = new EndpointConfig(
    "/api/payments",
    "FixedWindow",
    fixedWindowParams
);
```

## Thread Safety

- **ConcurrentHashMap**: Thread-safe state storage
- **Atomic operations**: Algorithm-specific atomicity
- **Immutable configuration**: No synchronization needed
- **Per-client isolation**: Independent state per (clientId, endpoint)

## Running the Demo

```bash
cd rate-limiter
javac -d out -sourcepath src src/Main.java
java -cp out Main
```

## Project Structure

```
rate-limiter/
├── src/
│   ├── com/lld/ratelimiter/
│   │   ├── domain/
│   │   │   ├── RateLimitResult.java
│   │   │   ├── EndpointConfig.java
│   │   │   └── ClientEndpointKey.java
│   │   ├── algorithm/
│   │   │   ├── RateLimitingAlgorithm.java
│   │   │   ├── AlgorithmState.java
│   │   │   └── impl/
│   │   │       ├── TokenBucketAlgorithm.java
│   │   │       ├── TokenBucketState.java
│   │   │       ├── SlidingWindowLogAlgorithm.java
│   │   │       ├── SlidingWindowLogState.java
│   │   │       ├── FixedWindowAlgorithm.java
│   │   │       └── FixedWindowState.java
│   │   ├── factory/
│   │   │   └── RateLimitingAlgorithmFactory.java
│   │   ├── repository/
│   │   │   ├── RateLimitStateRepository.java
│   │   │   └── InMemoryRateLimitStateRepository.java
│   │   ├── config/
│   │   │   └── ConfigurationService.java
│   │   ├── service/
│   │   │   └── RateLimiterService.java
│   │   └── facade/
│   │       ├── RateLimiterFacade.java
│   │       └── RateLimiterFacadeImpl.java
│   └── Main.java
├── DESIGN.md
└── README.md
```

## Key Design Decisions

1. **Strategy Pattern for Algorithms**: Easy to add new algorithms
2. **Factory Pattern**: Centralized algorithm creation
3. **ClientEndpointKey**: Type-safe composite key
4. **AlgorithmState Interface**: Type-safe state handling
5. **Immutable Configuration**: Thread-safe, loaded once
6. **Default Configuration**: Graceful fallback

## Extensibility

The design supports easy extension:
- **New algorithms**: Implement `RateLimitingAlgorithm` and `AlgorithmState`
- **New configuration sources**: Extend `ConfigurationService`
- **Distributed state**: Replace `InMemoryRateLimitStateRepository` with Redis/Database
- **Configuration reload**: Add reload mechanism to `ConfigurationService`

## Requirements Coverage

| Requirement | Status | Implementation |
|------------|--------|----------------|
| Configuration at startup | ✅ | ConfigurationService.loadConfiguration() |
| Requests with (clientId, endpoint) | ✅ | RateLimiterFacade.checkLimit() |
| Endpoint configuration | ✅ | EndpointConfig with algorithm + parameters |
| Algorithm-specific parameters | ✅ | Map<String, Object> parameters |
| Enforce rate limits | ✅ | RateLimiterService with algorithms |
| Structured result | ✅ | RateLimitResult (allowed, remaining, retryAfterMs) |
| Default limit | ✅ | Default EndpointConfig fallback |

