package com.example.blog.post.service;

import com.example.blog.common.exception.ResourceNotFoundException;
import com.example.blog.common.response.PageResponse;
import com.example.blog.post.domain.PostEntity;
import com.example.blog.post.dto.PostDetailResponse;
import com.example.blog.post.dto.PostFilterRequest;
import com.example.blog.post.dto.PostSummaryResponse;
import com.example.blog.post.dto.PostUpsertRequest;
import com.example.blog.post.mapper.PostMapper;
import com.example.blog.post.repository.PostRepository;
import com.example.blog.post.specification.PostSpecification;
import com.example.blog.security.CurrentUserService;
import com.example.blog.security.OwnershipAuthorizationService;
import com.example.blog.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final OwnershipAuthorizationService ownershipAuthorizationService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            OwnershipAuthorizationService ownershipAuthorizationService
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.ownershipAuthorizationService = ownershipAuthorizationService;
    }

    @Transactional
    @CacheEvict(cacheNames = {"posts", "postLists"}, allEntries = true)
    public PostDetailResponse createPost(PostUpsertRequest request) {
        var author = userRepository.getReferenceById(currentUserService.getCurrentUserId());
        PostEntity post = new PostEntity(
                request.title().trim(),
                request.content().trim(),
                request.category(),
                author
        );
        return PostMapper.toDetailResponse(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "posts", key = "#id")
    public PostDetailResponse getPost(Long id) {
        return PostMapper.toDetailResponse(getPostEntity(id));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "postLists", key = "#filter.cacheKey()")
    public PageResponse<PostSummaryResponse> listPosts(PostFilterRequest filter) {
        Specification<PostEntity> specification = PostSpecification.fromFilter(filter);
        PageRequest pageRequest = PageRequest.of(
                filter.resolvedPage(),
                filter.resolvedSize(),
                Sort.by(filter.resolvedSortDirection(), filter.resolvedSortBy())
        );
        Page<PostSummaryResponse> page = postRepository.findAll(specification, pageRequest)
                .map(PostMapper::toSummaryResponse);
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {"posts", "postLists"}, allEntries = true)
    public PostDetailResponse updatePost(Long id, PostUpsertRequest request) {
        PostEntity post = getPostEntity(id);
        ownershipAuthorizationService.assertCanModifyPost(post);
        post.update(request.title().trim(), request.content().trim(), request.category());
        return PostMapper.toDetailResponse(post);
    }

    @Transactional
    @CacheEvict(cacheNames = {"posts", "postLists"}, allEntries = true)
    public void deletePost(Long id) {
        PostEntity post = getPostEntity(id);
        ownershipAuthorizationService.assertCanModifyPost(post);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostEntity getPostEntity(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }
}
