package com.example.javabasis.dto;

import java.util.List;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,     // "Bearer"
        long expiresInMs,
        String username,
        List<String> roles) {
}
