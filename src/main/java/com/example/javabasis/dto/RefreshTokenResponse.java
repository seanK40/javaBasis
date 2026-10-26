package com.example.javabasis.dto;

public record RefreshTokenResponse(
        String token,
        String refreshToken,
        String tokenType,     // "Bearer"
        long expiresInMs) {
}
