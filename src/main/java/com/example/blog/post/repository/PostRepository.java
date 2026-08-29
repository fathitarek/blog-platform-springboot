package com.example.blog.post.repository;

import com.example.blog.post.domain.PostEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

    @EntityGraph(attributePaths = {"author", "comments", "comments.user"})
    Optional<PostEntity> findById(Long id);
}
