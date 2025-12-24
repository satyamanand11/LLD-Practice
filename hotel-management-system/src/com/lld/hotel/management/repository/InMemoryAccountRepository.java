package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Account;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Integer, Account> byId = new ConcurrentHashMap<>();
    private final Map<String, Account> byEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findById(int accountId) {
        return Optional.ofNullable(byId.get(accountId));
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email));
    }

    @Override
    public void save(Account account) {
        // Atomic update: both maps updated together
        // ConcurrentHashMap.put() is thread-safe
        byId.put(account.getAccountId(), account);
        byEmail.put(account.getEmail(), account);
    }
}
