```mermaid
classDiagram
    class KeyValueStore {
      + setPrimitive(key: String, value: Object) Result
      + setList(key: String, values: Collection) Result
      + setSet(key: String, values: Collection) Result
      + get(key: String) Result
      + deleteKey(key: String) Result
      + addToCollection(key: String, values: Collection) Result
      + fetchFromCollection(key: String, limit: int) Result
      + removeFromCollection(key: String, values: Collection) Result
    }

    class Command {
      + execute() Object
    }

    class CommandBus {
      + dispatch(cmd: Command) Object
    }

    class Result {
      + isOk() boolean
      + get() Object
      + error() String
    }

    class HolderType {
    }
    class PrimitiveKind {
    }

    class TypeDescriptor {
      - holder: HolderType
      - primitive: PrimitiveKind
      + holderType() HolderType
      + primitiveKind() PrimitiveKind
    }

    class Value {
      + type() TypeDescriptor
    }
    class PrimitiveValue {
      - value: Object
      + get() Object
    }
    class ListValue {
      - values: List
      + get() List
    }
    class SetValue {
      - values: Set
      + get() Set
    }

    class StoredEntry {
      + key: String
      + type: TypeDescriptor
      + payload: Object
    }

    class Storage {
      + read(key: String) StoredEntry
      + write(key: String, entry: StoredEntry)
      + delete(key: String)
    }
    class InMemoryStorage
    class ValueFactory {
      + createPrimitive(type: TypeDescriptor, data: Object) Value
      + create(type: TypeDescriptor, data: Collection) Value
    }
    class DefaultValueFactory
    class TypeEnforcer {
      + descriptorForPrimitive(value: Object) TypeDescriptor
      + descriptorForCollection(holder: HolderType, values: Collection) TypeDescriptor
      + ensureCompatible(existing: StoredEntry, incoming: TypeDescriptor)
    }
    class LockManager {
      + forKey(key: String) ReentrantReadWriteLock
    }
    class ReadLockGuard
    class WriteLockGuard

    class KeyValueStoreImpl {
      - storage: Storage
      - enforcer: TypeEnforcer
      - factory: ValueFactory
      - lockManager: LockManager
    }

    class SetPrimitiveCommand
    class SetListCommand
    class SetSetCommand
    class GetCommand
    class DeleteKeyCommand
    class AddToCollectionCommand
    class RemoveFromCollectionCommand
    class FetchFromCollectionCommand

    KeyValueStoreImpl ..|> KeyValueStore
    InMemoryStorage ..|> Storage
    DefaultValueFactory ..|> ValueFactory
    PrimitiveValue --|> Value
    ListValue --|> Value
    SetValue --|> Value

    Command <|-- SetPrimitiveCommand
    Command <|-- SetListCommand
    Command <|-- SetSetCommand
    Command <|-- GetCommand
    Command <|-- DeleteKeyCommand
    Command <|-- AddToCollectionCommand
    Command <|-- RemoveFromCollectionCommand
    Command <|-- FetchFromCollectionCommand

    CommandBus --> Command
    KeyValueStoreImpl --> Storage
    KeyValueStoreImpl --> TypeEnforcer
    KeyValueStoreImpl --> ValueFactory
    KeyValueStoreImpl --> LockManager
    TypeEnforcer --> TypeDescriptor
    Value --> TypeDescriptor