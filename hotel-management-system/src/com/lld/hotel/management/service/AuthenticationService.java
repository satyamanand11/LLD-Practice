package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.Account;
import com.lld.hotel.management.repository.AccountRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuthenticationService - Handles authentication logic
 * Separated from Account entity following SRP
 */
public class AuthenticationService {
    private final AccountRepository accountRepository;
    private final Map<String, String> passwordStore = new ConcurrentHashMap<>(); // email -> hashed password
    private final Map<String, Session> sessions = new ConcurrentHashMap<>(); // sessionId -> Session

    public AuthenticationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account register(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }

        // Check if account already exists
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Account with email already exists");
        }

        // Hash password (simplified - in production use BCrypt/Argon2)
        String hashedPassword = hashPassword(password);
        passwordStore.put(email, hashedPassword);

        // Account creation is handled by AccountService
        // This service only handles authentication
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Account creation failed"));
    }

    /**
     * Set password for an existing account
     * Used when account is created via AccountService first
     */
    public void setPassword(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }

        // Verify account exists
        if (!accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Account not found");
        }

        // Hash and store password
        String hashedPassword = hashPassword(password);
        passwordStore.put(email, hashedPassword);
    }

    public Session login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("password is required");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!account.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }

        String hashedPassword = passwordStore.get(email);
        if (hashedPassword == null || !verifyPassword(password, hashedPassword)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, account.getAccountId());
        sessions.put(sessionId, session);

        return session;
    }

    public void logout(String sessionId) {
        sessions.remove(sessionId);
    }

    public Optional<Account> validateSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null || session.isExpired()) {
            return Optional.empty();
        }
        return accountRepository.findById(session.getAccountId());
    }

    public void changePassword(int accountId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        String hashedOldPassword = passwordStore.get(account.getEmail());
        if (hashedOldPassword == null || !verifyPassword(oldPassword, hashedOldPassword)) {
            throw new IllegalArgumentException("Invalid old password");
        }

        String hashedNewPassword = hashPassword(newPassword);
        passwordStore.put(account.getEmail(), hashedNewPassword);
    }

    // Simplified password hashing (in production use BCrypt/Argon2)
    private String hashPassword(String password) {
        // This is a simplified hash - in production use proper hashing
        return String.valueOf(password.hashCode());
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }

    public static class Session {
        private final String sessionId;
        private final int accountId;
        private final long createdAt;
        private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000; // 24 hours

        public Session(String sessionId, int accountId) {
            this.sessionId = sessionId;
            this.accountId = accountId;
            this.createdAt = System.currentTimeMillis();
        }

        public String getSessionId() {
            return sessionId;
        }

        public int getAccountId() {
            return accountId;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > SESSION_TIMEOUT_MS;
        }
    }
}

