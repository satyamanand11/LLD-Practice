package com.lld.kvstore;

import com.lld.kvstore.core.KeyValueStore;
import com.lld.kvstore.core.KeyValueStoreImpl;
import com.lld.kvstore.types.Value;
import com.lld.kvstore.types.Result;
import com.lld.kvstore.storage.*;
import com.lld.kvstore.commands.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Storage storage = new InMemoryStorage();
        TypeValidator typeValidator = new TypeValidator();
        ValueFactory valueFactory = new ValueFactory();
        ConcurrencyManager concurrencyManager = new ConcurrencyManager();
        
        KeyValueStore store = new KeyValueStoreImpl(storage, typeValidator, valueFactory, concurrencyManager);
        CommandBus commandBus = new CommandBus();
        
        System.out.println("=== Key-Value Store Demo ===");
        
        testPrimitiveOperations(store);
        testListOperations(store);
        testSetOperations(store);
        testCommandPattern(commandBus, store);
        testTypeSafety(store);
    }
    
    private static void testPrimitiveOperations(KeyValueStore store) {
        System.out.println("\n--- Primitive Operations ---");
        
        Result<Void> putResult = store.setPrimitive("name", "John Doe");
        System.out.println("Set primitive: " + putResult);
        
        Result<Value> getResult = store.get("name");
        System.out.println("Get primitive: " + getResult);
        
        Result<Void> putNumber = store.setPrimitive("age", 25);
        System.out.println("Set number: " + putNumber);
        
        Result<Value> getNumber = store.get("age");
        System.out.println("Get number: " + getNumber);
    }
    
    private static void testListOperations(KeyValueStore store) {
        System.out.println("\n--- List Operations ---");
        
        List<String> fruits = Arrays.asList("apple", "banana", "orange");
        Result<Void> setList = store.setList("fruits", fruits);
        System.out.println("Set list: " + setList);
        
        Result<Value> getList = store.get("fruits");
        System.out.println("Get list: " + getList);
        
        List<String> moreFruits = Arrays.asList("grape", "mango");
        Result<Void> addToList = store.addToCollection("fruits", moreFruits);
        System.out.println("Add to list: " + addToList);
        
        Result<java.util.Collection<?>> fetchList = store.fetchFromCollection("fruits", 3);
        System.out.println("Fetch from list (limit 3): " + fetchList);
    }
    
    private static void testSetOperations(KeyValueStore store) {
        System.out.println("\n--- Set Operations ---");
        
        Set<String> colors = Set.of("red", "green", "blue");
        Result<Void> setSet = store.setSet("colors", colors);
        System.out.println("Set set: " + setSet);
        
        Result<Value> getSet = store.get("colors");
        System.out.println("Get set: " + getSet);
        
        Set<String> moreColors = Set.of("yellow", "purple");
        Result<Void> addToSet = store.addToCollection("colors", moreColors);
        System.out.println("Add to set: " + addToSet);
        
        Result<java.util.Collection<?>> fetchSet = store.fetchFromCollection("colors", 0);
        System.out.println("Fetch from set: " + fetchSet);
    }
    
    private static void testCommandPattern(CommandBus commandBus, KeyValueStore store) {
        System.out.println("\n--- Command Pattern ---");
        
        Command setCommand = new SetPrimitiveCommand(store, "command_test", "Hello Command!");
        Object result1 = commandBus.dispatch(setCommand);
        System.out.println("Command result: " + result1);
        
        Command getCommand = new GetCommand(store, "command_test");
        Object result2 = commandBus.dispatch(getCommand);
        System.out.println("Get command result: " + result2);
        
        Command deleteCommand = new DeleteCommand(store, "command_test");
        Object result3 = commandBus.dispatch(deleteCommand);
        System.out.println("Delete command result: " + result3);
    }
    
    private static void testTypeSafety(KeyValueStore store) {
        System.out.println("\n--- Type Safety Test ---");
        
        store.setPrimitive("test_key", "string_value");
        System.out.println("Set string value");
        
        try {
            store.setPrimitive("test_key", 123);
            System.out.println("ERROR: Should have failed due to type mismatch");
        } catch (Exception e) {
            System.out.println("Type safety working: " + e.getMessage());
        }
        
        store.deleteKey("test_key");
        System.out.println("Cleaned up test key");
    }
}
