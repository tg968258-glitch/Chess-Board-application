package com.chess.chessboard.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class PlayerAccountService {
    private final InMemoryUserDetailsManager users;
    private final PasswordEncoder passwordEncoder;

    public PlayerAccountService(InMemoryUserDetailsManager users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public synchronized RegistrationResult register(String username, String password) {
        if (username == null || !username.matches("[A-Za-z0-9]{3,20}")) {
            return RegistrationResult.failure("Username must contain 3–20 letters or numbers.");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            return RegistrationResult.failure("Password must contain 8–72 characters.");
        }
        if (users.userExists(username)) {
            return RegistrationResult.failure("That username is already registered.");
        }

        users.createUser(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("PLAYER")
                .build());
        return RegistrationResult.success();
    }

    public record RegistrationResult(boolean successful, String message) {
        static RegistrationResult success() { return new RegistrationResult(true, "Account created."); }
        static RegistrationResult failure(String message) { return new RegistrationResult(false, message); }
    }
}
