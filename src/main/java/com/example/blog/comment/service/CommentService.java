package com.example.blog.comment.service;

import com.example.blog.comment.domain.CommentEntity;
import com.example.blog.comment.dto.CommentCreateRequest;
import com.example.blog.comment.dto.CommentResponse;
import com.example.blog.comment.mapper.CommentMapper;
import com.example.blog.comment.repository.CommentRepository;
import com.example.blog.common.exception.ResourceNotFoundException;
import com.example.blog.post.repository.PostRepository;
import com.example.blog.post.service.PostService;
import com.example.blog.security.CurrentUserService;
import com.example.blog.security.OwnershipAuthorizationService;
import com.example.blog.user.repository.UserRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final OwnershipAuthorizationService ownershipAuthorizationService;

    public CommentService(
            CommentRepository commentRepository,
            PostService postService,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            OwnershipAuthorizationService ownershipAuthorizationService
    ) {
        this.commentRepository = commentRepository;
        this.postService = postService;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.ownershipAuthorizationService = ownershipAuthorizationService;
    }

    @Transactional
    @CacheEvict(cacheNames = {"posts", "postLists"}, allEntries = true)
    public CommentResponse addComment(Long postId, CommentCreateRequest request) {
        var post = postService.getPostEntity(postId);
        var user = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        CommentEntity comment = new CommentEntity(request.content().trim(), post, user);
        return CommentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId) {
        postService.getPostEntity(postId);
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(CommentMapper::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = {"posts", "postLists"}, allEntries = true)
    public void deleteComment(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        ownershipAuthorizationService.assertCanDeleteComment(comment);
        commentRepository.delete(comment);
    }
}
