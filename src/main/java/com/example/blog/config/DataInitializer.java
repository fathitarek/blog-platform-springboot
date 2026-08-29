package com.example.blog.config;

import com.example.blog.user.domain.RoleEntity;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.repository.RoleRepository;
import java.util.EnumSet;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName roleName : EnumSet.allOf(RoleName.class)) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new RoleEntity(roleName)));
        }
    }
}
