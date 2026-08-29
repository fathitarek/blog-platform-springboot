package com.example.blog.comment.mapper;

import com.example.blog.comment.domain.CommentEntity;
import com.example.blog.comment.dto.CommentResponse;
import com.example.blog.user.mapper.UserMapper;

public final class CommentMapper {

    private CommentMapper() {
    }

    public static CommentResponse toResponse(CommentEntity comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                UserMapper.toSummaryResponse(comment.getUser())
        );
    }
}
