package com.example.blog.security;

import com.example.blog.comment.domain.CommentEntity;
import com.example.blog.common.exception.ForbiddenOperationException;
import com.example.blog.post.domain.PostEntity;
import org.springframework.stereotype.Service;

@Service
public class OwnershipAuthorizationService {

    private final CurrentUserService currentUserService;

    public OwnershipAuthorizationService(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public void assertCanModifyPost(PostEntity post) {
        if (currentUserService.isAdmin() || post.getAuthor().getId().equals(currentUserService.getCurrentUserId())) {
            return;
        }
        throw new ForbiddenOperationException("You are not allowed to modify this post");
    }

    public void assertCanDeleteComment(CommentEntity comment) {
        if (currentUserService.isAdmin() || comment.getUser().getId().equals(currentUserService.getCurrentUserId())) {
            return;
        }
        throw new ForbiddenOperationException("You are not allowed to delete this comment");
    }
}
