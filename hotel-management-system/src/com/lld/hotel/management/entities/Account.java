package com.lld.hotel.management.entities;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Account {

    private final int accountId;
    private final String name;
    private final String email;
    private final Instant createdAt;

    private final Set<Role> roles = new HashSet<>();
    private boolean active;

    public int getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Account(int accountId, String name, String email, Set<Role> initialRoles) {

        if (accountId <= 0) {
            throw new IllegalArgumentException("accountId must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (initialRoles == null || initialRoles.isEmpty()) {
            throw new IllegalArgumentException("at least one role is required");
        }

        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.roles.addAll(initialRoles);
        this.active = true;
        this.createdAt = Instant.now();
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (!roles.contains(role)) {
            throw new IllegalStateException("role not assigned: " + role);
        }
        if (roles.size() == 1) {
            throw new IllegalStateException("account must have at least one role");
        }
        roles.remove(role);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

    public Set<Role> roles() {
        return Collections.unmodifiableSet(roles);
    }
}