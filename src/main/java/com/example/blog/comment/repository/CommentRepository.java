package com.example.blog.comment.repository;

import com.example.blog.comment.domain.CommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByPost_IdOrderByCreatedAtAsc(Long postId);
}
