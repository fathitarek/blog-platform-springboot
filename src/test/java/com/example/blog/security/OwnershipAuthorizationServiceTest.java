package com.example.blog.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.blog.comment.domain.CommentEntity;
import com.example.blog.common.exception.ForbiddenOperationException;
import com.example.blog.post.domain.Category;
import com.example.blog.post.domain.PostEntity;
import com.example.blog.user.domain.RoleEntity;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.domain.UserEntity;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OwnershipAuthorizationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    private OwnershipAuthorizationService ownershipAuthorizationService;

    @BeforeEach
    void setUp() {
        ownershipAuthorizationService = new OwnershipAuthorizationService(currentUserService);
    }

    @Test
    void adminCanModifyAnyPost() {
        when(currentUserService.isAdmin()).thenReturn(true);
        PostEntity post = buildPost(12L, 7L);
        assertDoesNotThrow(() -> ownershipAuthorizationService.assertCanModifyPost(post));
    }

    @Test
    void ownerCanModifyOwnPost() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(7L);
        PostEntity post = buildPost(12L, 7L);
        assertDoesNotThrow(() -> ownershipAuthorizationService.assertCanModifyPost(post));
    }

    @Test
    void nonOwnerCannotModifyPost() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(8L);
        PostEntity post = buildPost(12L, 7L);
        assertThrows(ForbiddenOperationException.class, () -> ownershipAuthorizationService.assertCanModifyPost(post));
    }

    @Test
    void nonOwnerCannotDeleteComment() {
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(8L);
        CommentEntity comment = buildComment(44L, 7L);
        assertThrows(ForbiddenOperationException.class, () -> ownershipAuthorizationService.assertCanDeleteComment(comment));
    }

    private PostEntity buildPost(Long postId, Long authorId) {
        UserEntity author = new UserEntity("Author", "author@example.com", "password", Set.of(new RoleEntity(RoleName.AUTHOR)));
        ReflectionTestUtils.setField(author, "id", authorId);
        PostEntity post = new PostEntity("Title", "Content", Category.TECHNOLOGY, author);
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private CommentEntity buildComment(Long commentId, Long userId) {
        UserEntity user = new UserEntity("User", "user@example.com", "password", Set.of(new RoleEntity(RoleName.AUTHOR)));
        ReflectionTestUtils.setField(user, "id", userId);
        UserEntity author = new UserEntity("Author", "author@example.com", "password", Set.of(new RoleEntity(RoleName.AUTHOR)));
        ReflectionTestUtils.setField(author, "id", 7L);
        PostEntity post = new PostEntity("Title", "Content", Category.TECHNOLOGY, author);
        CommentEntity comment = new CommentEntity("Nice post", post, user);
        ReflectionTestUtils.setField(comment, "id", commentId);
        return comment;
    }
}
