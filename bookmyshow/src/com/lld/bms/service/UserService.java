package com.lld.bms.service;

import com.lld.bms.domain.User;
import com.lld.bms.repo.UserRepository;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
    }

    public User registerUser(String name, String email) {
        validateNonBlank(name, "name");
        validateNonBlank(email, "email");
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered: " + email);
        });
        User user = new User(UUID.randomUUID().toString(), name, email);
        userRepository.save(user);
        return user;
    }

    public User getUser(String id) {
        validateNonBlank(id, "id");
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    public Optional<User> findByEmail(String email) {
        validateNonBlank(email, "email");
        return userRepository.findByEmail(email);
    }

    private static void validateNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
    }
}
