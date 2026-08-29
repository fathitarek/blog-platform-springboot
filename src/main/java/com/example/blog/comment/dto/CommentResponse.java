package com.example.blog.comment.dto;

import com.example.blog.user.dto.UserSummaryResponse;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        LocalDateTime createdAt,
        UserSummaryResponse user
) {
}
