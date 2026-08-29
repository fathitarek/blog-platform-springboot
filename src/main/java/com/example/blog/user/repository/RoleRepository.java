package com.example.blog.user.repository;

import com.example.blog.user.domain.RoleEntity;
import com.example.blog.user.domain.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleName name);
}
