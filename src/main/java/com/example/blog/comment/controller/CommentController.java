package com.example.blog.comment.controller;

import com.example.blog.comment.dto.CommentCreateRequest;
import com.example.blog.comment.dto.CommentResponse;
import com.example.blog.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(postId, request));
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
