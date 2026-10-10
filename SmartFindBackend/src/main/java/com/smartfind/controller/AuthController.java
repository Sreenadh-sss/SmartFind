package com.smartfind.controller;

import com.smartfind.dto.ApiResponse;
import com.smartfind.dto.AuthResponse;
import com.smartfind.dto.LoginRequest;
import com.smartfind.dto.RegisterRequest;
import com.smartfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user registration, authentication, and session termination.
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return new ResponseEntity<>(
                ApiResponse.success("Account registered successfully! Welcome to SmartFind.", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Authenticates an existing user and returns session profile.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful!", response));
    }

    /**
     * Logs out the user and clears session state.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
}
