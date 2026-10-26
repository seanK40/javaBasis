package com.example.javabasis.controller;

import com.example.javabasis.dto.*;
import com.example.javabasis.service.UserLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public endpoints (permitAll in SecurityConfig): login, register, refresh, logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    // POST /api/auth/login   {"username":"admin","password":"Admin123!"}
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userLoginService.login(request);
    }

    // POST /api/auth/refresh   {"refreshToken":"..."}
    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return userLoginService.refreshToken(request);
    }

    // POST /api/auth/logout    {"refreshToken":"..."}
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.refreshToken() != null) {
            userLoginService.logout(request.refreshToken());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // POST /api/auth/register   {"username":"dana","password":"Dana1234!"}
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userLoginService.register(request);
    }
}
