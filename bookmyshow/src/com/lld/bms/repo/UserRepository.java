package com.lld.bms.repo;

import com.lld.bms.domain.User;

import java.util.Optional;

public interface UserRepository extends Repository<User, String> {
    Optional<User> findByEmail(String email);
}
