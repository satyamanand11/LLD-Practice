package com.lld.hotel.management.service;

import com.lld.hotel.management.entities.Account;
import com.lld.hotel.management.entities.Role;
import com.lld.hotel.management.repository.AccountRepository;

import java.util.Optional;
import java.util.Set;

/**
 * AccountService - Manages account data and profile operations
 * Separated from AuthenticationService following SRP
 */
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccount(int accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public Account updateProfile(int accountId, String name) {
        Account account = getAccount(accountId);
        
        // In a real system, we'd have a method to update name
        // For now, we'll return the account as-is
        // This would require making Account mutable or using a different approach
        return account;
    }

    public void deactivateAccount(int accountId) {
        Account account = getAccount(accountId);
        account.deactivate();
        accountRepository.save(account);
    }

    public void activateAccount(int accountId) {
        Account account = getAccount(accountId);
        account.activate();
        accountRepository.save(account);
    }

    public Account createAccount(String name, String email, Set<Role> roles) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("at least one role is required");
        }

        // Check if account already exists
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Account with email already exists");
        }

        Account account = new Account(
                com.lld.hotel.management.entities.IdGenerator.nextId(),
                name,
                email,
                roles
        );

        accountRepository.save(account);
        return account;
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
}

