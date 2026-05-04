package com.lld.bms.repo;

import com.lld.bms.domain.User;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {
    private final ConcurrentHashMap<String, User> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> emailIndex = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        store.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user.getId());
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String id = emailIndex.get(email);
        return id == null ? Optional.empty() : Optional.ofNullable(store.get(id));
    }
}
