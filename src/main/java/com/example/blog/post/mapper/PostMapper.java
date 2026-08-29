package com.example.blog.post.mapper;

import com.example.blog.comment.mapper.CommentMapper;
import com.example.blog.post.domain.PostEntity;
import com.example.blog.post.dto.PostDetailResponse;
import com.example.blog.post.dto.PostSummaryResponse;
import com.example.blog.user.mapper.UserMapper;

public final class PostMapper {

    private PostMapper() {
    }

    public static PostSummaryResponse toSummaryResponse(PostEntity post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                UserMapper.toSummaryResponse(post.getAuthor())
        );
    }

    public static PostDetailResponse toDetailResponse(PostEntity post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                UserMapper.toSummaryResponse(post.getAuthor()),
                post.getComments().stream().map(CommentMapper::toResponse).toList()
        );
    }
}
