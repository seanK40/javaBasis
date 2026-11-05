package com.example.javabasis.service;

import com.example.javabasis.repository.BlacklistedTokenRepository;
import com.example.javabasis.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void blacklist(String accessToken) {
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(accessToken);
        } catch (JwtException | IllegalArgumentException e) {
            return; // already expired, forged or malformed: it can't be used anyway
        }

        String jti = claims.getId();
        if (jti == null) {
            return; // token from before this change (no jti): it expires within the hour
        }

        repository.deleteExpired(Instant.now());                              // 1. clean while writing
        repository.insertIfAbsent(jti, claims.getExpiration().toInstant());  // 2. add this token
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return jti != null && repository.existsByJti(jti);
    }
}