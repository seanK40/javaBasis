package com.example.javabasis.dto;
import com.example.javabasis.dto.UserResponse;
import com.example.javabasis.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String username,
        Set<String> roles
) {
    public static UserResponse from(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                roleNames
        );
    }
}