package com.example.blog.post.dto;

import com.example.blog.comment.dto.CommentResponse;
import com.example.blog.post.domain.Category;
import com.example.blog.user.dto.UserSummaryResponse;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        Category category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UserSummaryResponse author,
        List<CommentResponse> comments
) {
}
