# Authentication Design Decision

## Question
Should Account entity have authentication responsibility or should it be in an AccountManager/AuthenticationService class?

## Answer: **Separate AuthenticationService is Better**

## Design Decision

### ✅ Recommended Approach: AuthenticationService

```
Account Entity (Domain)
├── accountId
├── name
├── email
├── phone
├── accountType
├── isActive
└── Domain logic only

AuthenticationService (Service Layer)
├── login(email, password)
├── logout(sessionId)
├── register(accountInfo)
├── changePassword(...)
├── validateSession(...)
└── Security logic only
```

## Why Separate Authentication?

### 1. **Single Responsibility Principle (SRP)**
- **Account Entity**: Responsible for account data and domain state
- **AuthenticationService**: Responsible for security, authentication, and session management
- Each class has one reason to change

### 2. **Separation of Concerns**
- **Domain Layer (Account)**: Represents business concept
- **Service Layer (AuthenticationService)**: Handles infrastructure/security concerns
- Authentication is a cross-cutting concern, not a domain concept

### 3. **Testability**
```java
// Easy to mock authentication for testing
AuthenticationService authService = mock(AuthenticationService.class);
when(authService.login(any(), any())).thenReturn(session);

// Account entity can be tested independently
Account account = new Account(...);
// Test account domain logic without authentication complexity
```

### 4. **Security & Maintainability**
- Centralized authentication logic
- Easier to implement security best practices (password hashing, rate limiting, etc.)
- Single place to audit authentication attempts
- Easier to add features like 2FA, OAuth, etc.

### 5. **Flexibility**
- Can swap authentication mechanisms without changing Account entity
- Support multiple auth methods (JWT, OAuth, SAML) via Strategy pattern
- Easy to add session management, token refresh, etc.

### 6. **Domain-Driven Design (DDD) Best Practices**
- Entities should represent domain concepts, not infrastructure
- Authentication is infrastructure, not domain logic
- Keeps domain model clean and focused

## Comparison

### ❌ Bad: Authentication in Account Entity
```java
public class Account {
    private String email;
    private String passwordHash;
    
    public boolean authenticate(String password) {
        // Password hashing logic
        // Session creation
        // Token generation
        // Security checks
    }
    
    public void changePassword(String oldPassword, String newPassword) {
        // Validation
        // Hashing
        // Update logic
    }
}
```

**Problems:**
- Account entity becomes bloated with infrastructure concerns
- Hard to test (need to mock security components)
- Violates SRP
- Difficult to change authentication mechanism
- Mixes domain and infrastructure logic

### ✅ Good: Separate AuthenticationService
```java
// Domain Entity - Clean and focused
public class Account {
    private String accountId;
    private String name;
    private String email;
    private AccountType accountType;
    private boolean isActive;
    
    // Only domain logic
    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
}

// Service Layer - Handles authentication
public class AuthenticationService {
    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private SessionManager sessionManager;
    
    public Session login(String email, String password) {
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isActive()) {
            throw new AuthenticationException();
        }
        
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new AuthenticationException();
        }
        
        return sessionManager.createSession(account);
    }
    
    public void logout(String sessionId) {
        sessionManager.invalidateSession(sessionId);
    }
}
```

**Benefits:**
- Clear separation of concerns
- Easy to test
- Follows SOLID principles
- Flexible and extensible
- Maintainable

## Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Controllers, API Endpoints)          │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Service Layer                   │
│  ┌──────────────────────────────────┐  │
│  │   AuthenticationService          │  │
│  │   - login()                      │  │
│  │   - logout()                     │  │
│  │   - register()                   │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │   AccountService                 │  │
│  │   - getAccount()                 │  │
│  │   - updateProfile()              │  │
│  └──────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Domain Layer                    │
│  ┌──────────────────────────────────┐  │
│  │   Account (Entity)               │  │
│  │   - accountId                    │  │
│  │   - name, email, phone           │  │
│  │   - accountType                  │  │
│  │   - Domain logic only            │  │
│  └──────────────────────────────────┘  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Repository Layer                │
│  - AccountRepository                    │
│  - SessionRepository                    │
└─────────────────────────────────────────┘
```

## Implementation Notes

### Account Entity
- **No password field** (stored separately for security)
- **No authentication methods**
- **Only domain logic**: activation, deactivation, profile updates

### AuthenticationService
- Handles password hashing (using PasswordEncoder)
- Manages sessions/tokens
- Validates credentials
- Handles security concerns (rate limiting, brute force protection)

### AccountService
- Manages account data operations
- Profile updates
- Account lifecycle (activate/deactivate)
- No authentication logic

## Conclusion

**Separating authentication into AuthenticationService is the better design** because it:
1. Follows SOLID principles (especially SRP)
2. Maintains clean domain model (DDD)
3. Improves testability
4. Enhances security and maintainability
5. Provides flexibility for future changes

This is a standard practice in enterprise applications and aligns with industry best practices.

