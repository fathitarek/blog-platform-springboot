package com.example.blog.user.mapper;

import com.example.blog.user.domain.UserEntity;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.dto.UserResponse;
import com.example.blog.user.dto.UserSummaryResponse;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet())
        );
    }

    public static UserSummaryResponse toSummaryResponse(UserEntity user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail());
    }
}
