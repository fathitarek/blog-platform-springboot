package com.example.blog.post.controller;

import com.example.blog.comment.dto.CommentResponse;
import com.example.blog.common.response.PageResponse;
import com.example.blog.post.dto.PostDetailResponse;
import com.example.blog.post.dto.PostFilterRequest;
import com.example.blog.post.dto.PostSummaryResponse;
import com.example.blog.post.dto.PostUpsertRequest;
import com.example.blog.post.service.PostService;
import com.example.blog.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody PostUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping
    public PageResponse<PostSummaryResponse> listPosts(@ModelAttribute PostFilterRequest filter) {
        return postService.listPosts(filter);
    }

    @GetMapping("/{id}")
    public PostDetailResponse getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PostDetailResponse updatePost(@PathVariable Long id, @Valid @RequestBody PostUpsertRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> getComments(@PathVariable Long id) {
        return commentService.getCommentsByPost(id);
    }
}
