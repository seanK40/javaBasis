package com.example.javabasis.service;

import com.example.javabasis.dto.RefreshTokenRequest;
import com.example.javabasis.dto.RefreshTokenResponse;
import com.example.javabasis.entity.RefreshToken;
import com.example.javabasis.entity.User;
import com.example.javabasis.exception.TokenRefreshException;
import com.example.javabasis.repository.RefreshTokenRepository;
import com.example.javabasis.repository.UserRepository;
import com.example.javabasis.security.CustomUserDetailsService;
import com.example.javabasis.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenRefreshException("Refresh token is invalid"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token was expired or revoked. Please log in again");
        }

        // Token rotation: issue a new refresh token on each refresh
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken = refreshTokenRepository.save(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
        String newAccessToken = jwtUtil.generateToken(userDetails);

        return new RefreshTokenResponse(
                newAccessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtUtil.getExpirationMs()
        );
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}
