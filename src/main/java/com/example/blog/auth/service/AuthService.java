package com.example.blog.auth.service;

import com.example.blog.auth.dto.AuthResponse;
import com.example.blog.auth.dto.LoginRequest;
import com.example.blog.auth.dto.RegisterRequest;
import com.example.blog.config.AppProperties;
import com.example.blog.common.exception.DuplicateResourceException;
import com.example.blog.security.JwtService;
import com.example.blog.security.UserPrincipal;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.domain.UserEntity;
import com.example.blog.user.dto.UserResponse;
import com.example.blog.user.mapper.UserMapper;
import com.example.blog.user.repository.RoleRepository;
import com.example.blog.user.repository.UserRepository;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        UserEntity user = new UserEntity(
                request.name().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                Set.of(roleRepository.findByName(RoleName.AUTHOR)
                        .orElseThrow(() -> new IllegalStateException("AUTHOR role must exist")))
        );
        UserEntity saved = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.from(saved);
        return new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                appProperties.jwt().expirationMinutes(),
                UserMapper.toResponse(saved)
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserEntity user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                appProperties.jwt().expirationMinutes(),
                UserMapper.toResponse(user)
        );
    }
}
