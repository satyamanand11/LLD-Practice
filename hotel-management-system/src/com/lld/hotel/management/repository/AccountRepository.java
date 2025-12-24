package com.lld.hotel.management.repository;

import com.lld.hotel.management.entities.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(int accountId);
    Optional<Account> findByEmail(String email);
    void save(Account account);
}
