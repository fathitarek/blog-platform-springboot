package com.example.blog.auth.dto;

import com.example.blog.user.dto.UserResponse;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMinutes,
        UserResponse user
) {
}
