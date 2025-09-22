# Key-Value Store - Clean Architecture

A clean, interview-friendly Key-Value store implementation with meaningful package names and clear separation of concerns.

## Problem Statement

Design and implement a Key-Value (KV) data store that can perform the following operations:

- **Key**: Always a string that uniquely identifies an entry
- **Value Types**: 
  - **Primitives**: String, Number (Integer, Long, Double, Float), Boolean
  - **Collections**: List of primitives (Ordered), Set of primitives (Unordered unique)

### Use Cases

#### Primitives
- Store a value against a key
- Fetch the value stored against a given key
- Delete a key

#### Collections
- Store a single value or multiple values against a key
- Fetch a single value or multiple values against a key
- Delete a single value or multiple values against a key
- Delete a key

#### Type Safety
- The data type of the value/primitive is determined at the time of first insert
- The data type of the value/collection along with the holder primitive type is determined at the time of first insert
- For subsequent updates to the key, the type safety must be enforced

## Architecture

### Design Patterns Used

1. **Command Pattern** - Encapsulates operations as objects (similar to Redis)
2. **Factory Pattern** - Creates value objects based on type descriptors
3. **Strategy Pattern** - Type validation and enforcement strategies
4. **Observer Pattern** - Can be easily extended for event notifications

### SOLID Principles Applied

- **Single Responsibility** - Each class has one clear purpose
- **Open/Closed** - Easy to extend with new value types or commands
- **Interface Segregation** - Clean, focused interfaces
- **Dependency Inversion** - Depends on abstractions, not concretions

## Package Structure

```
src/com/lld/kvstore/
├── core/                         # Core Business Logic
│   ├── KeyValueStore.java        # Main store interface
│   └── KeyValueStoreImpl.java    # Main store implementation
├── types/                        # Type System
│   ├── ValueType.java            # Enum: PRIMITIVE, LIST, SET
│   ├── PrimitiveType.java        # Enum: STRING, INTEGER, LONG, etc.
│   ├── TypeDescriptor.java       # Type information holder
│   ├── Value.java                # Abstract value class
│   ├── PrimitiveValue.java       # Primitive value implementation
│   ├── ListValue.java            # List value implementation
│   ├── SetValue.java             # Set value implementation
│   └── Result.java               # Result wrapper
├── storage/                      # Storage Layer
│   ├── StorageEntry.java         # Storage entry wrapper
│   ├── Storage.java              # Storage interface
│   ├── InMemoryStorage.java      # In-memory storage implementation
│   ├── TypeValidator.java        # Type validation and enforcement
│   ├── ValueFactory.java         # Value factory
│   └── ConcurrencyManager.java   # Concurrency management
├── commands/                     # Command Pattern
│   ├── Command.java              # Command interface
│   ├── CommandBus.java           # Command dispatcher
│   ├── SetPrimitiveCommand.java  # Set primitive command
│   ├── GetCommand.java           # Get command
│   └── DeleteCommand.java        # Delete command
└── Main.java                     # Demo application
```

## Key Features

### 1. Type Safety
- **Type Descriptor**: Encapsulates both value type (PRIMITIVE, LIST, SET) and primitive type
- **Type Validator**: Validates type compatibility on every operation
- **Immutable Types**: Once set, the type cannot be changed for a key

### 2. Thread Safety
- **Concurrency Manager**: Provides per-key locking for concurrent access
- **Read-Write Locks**: Optimized for read-heavy workloads
- **Thread-Safe Collections**: All collections are thread-safe

### 3. Command Pattern
- **Encapsulated Operations**: Each operation is a command object
- **Command Bus**: Centralized command execution
- **Extensible**: Easy to add new command types

### 4. Clean Architecture
- **Layered Design**: Clear separation of concerns
- **Meaningful Packages**: Easy to understand package structure
- **Interface-Based**: Easy to mock and test

## Usage Examples

### Basic Operations

```java
// Create store instance
Storage storage = new InMemoryStorage();
TypeValidator typeValidator = new TypeValidator();
ValueFactory valueFactory = new ValueFactory();
ConcurrencyManager concurrencyManager = new ConcurrencyManager();
KeyValueStore store = new KeyValueStoreImpl(storage, typeValidator, valueFactory, concurrencyManager);

// Primitive operations
store.setPrimitive("name", "John Doe");
Result<Value> result = store.get("name");

// List operations
List<String> fruits = Arrays.asList("apple", "banana");
store.setList("fruits", fruits);
store.addToCollection("fruits", Arrays.asList("orange"));

// Set operations
Set<String> colors = Set.of("red", "green");
store.setSet("colors", colors);
```

### Command Pattern Usage

```java
CommandBus commandBus = new CommandBus();

// Execute commands
Command setCommand = new SetPrimitiveCommand(store, "key", "value");
Object result = commandBus.dispatch(setCommand);

Command getCommand = new GetCommand(store, "key");
Object value = commandBus.dispatch(getCommand);
```

## Type Safety Example

```java
// First insert determines type
store.setPrimitive("test", "string_value");  // Type: PRIMITIVE + STRING

// This will fail - type mismatch
store.setPrimitive("test", 123);  // Throws IllegalArgumentException

// Collections also enforce type safety
store.setList("numbers", Arrays.asList(1, 2, 3));  // Type: LIST + INTEGER
store.setList("numbers", Arrays.asList("a", "b"));  // Throws IllegalArgumentException
```

## Running the Demo

```bash
cd key-value-store
javac -d out src/com/lld/kvstore/**/*.java
java -cp out com.lld.kvstore.Main
```

## Design Benefits

1. **Interview Friendly**: Clean, simple design that's easy to explain
2. **Meaningful Names**: Package and class names clearly indicate purpose
3. **Extensible**: Easy to add new value types or operations
4. **Testable**: Clear interfaces make unit testing straightforward
5. **Maintainable**: Well-organized code with clear responsibilities
6. **Type Safe**: Prevents runtime type errors
7. **Thread Safe**: Handles concurrent access properly

## Package Naming Convention

- **`core`**: Core business logic and main interfaces
- **`types`**: Type system and value representations
- **`storage`**: Storage layer and data persistence
- **`commands`**: Command pattern implementation

## Future Enhancements

- **Persistence**: Add disk-based storage
- **Serialization**: JSON/XML serialization support
- **Observers**: Event notification system
- **Metrics**: Performance monitoring
- **Configuration**: External configuration support