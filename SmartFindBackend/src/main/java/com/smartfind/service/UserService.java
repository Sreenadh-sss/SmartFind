package com.smartfind.service;

import com.smartfind.dto.AuthResponse;
import com.smartfind.dto.LoginRequest;
import com.smartfind.dto.RegisterRequest;
import com.smartfind.entity.User;
import com.smartfind.exception.BadRequestException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service handling user account registration, authentication, and session handling.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with BCrypt hashed password and uniqueness validation.
     */
    public AuthResponse registerUser(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with email '" + normalizedEmail + "' already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));

        String requestedRole = request.getRole() != null ? request.getRole().trim().toUpperCase() : "USER";
        user.setRole("ADMIN".equals(requestedRole) ? "ADMIN" : "USER");

        User savedUser = userRepository.save(user);

        // Generate session token
        String sessionToken = UUID.randomUUID().toString();

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                sessionToken
        );
    }

    /**
     * Authenticates existing user by verifying email and BCrypt hashed password.
     */
    public AuthResponse loginUser(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        // Generate session token
        String sessionToken = UUID.randomUUID().toString();

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                sessionToken
        );
    }

    /**
     * Finds a user by ID or throws ResourceNotFoundException.
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
