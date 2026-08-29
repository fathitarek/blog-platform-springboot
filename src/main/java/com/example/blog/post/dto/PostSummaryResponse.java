package com.example.blog.post.dto;

import com.example.blog.post.domain.Category;
import com.example.blog.user.dto.UserSummaryResponse;
import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String content,
        Category category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserSummaryResponse author
) {
}
