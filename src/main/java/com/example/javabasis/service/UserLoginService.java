package com.example.javabasis.service;

import com.example.javabasis.dto.*;
import com.example.javabasis.entity.RefreshToken;
import com.example.javabasis.entity.Role;
import com.example.javabasis.entity.RoleName;
import com.example.javabasis.entity.User;
import com.example.javabasis.exception.UsernameTakenException;
import com.example.javabasis.repository.RoleRepository;
import com.example.javabasis.repository.UserRepository;
import com.example.javabasis.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserLoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new LoginResponse(token, refreshToken.getToken(), "Bearer", jwtUtil.getExpirationMs(),
                userDetails.getUsername(), roles);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.refreshAccessToken(request);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameTakenException("Username is already taken: " + request.username());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing - did DataInitializer run?"));

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        return UserResponse.from(userRepository.save(user));
    }
}
