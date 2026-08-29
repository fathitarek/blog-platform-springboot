package com.example.blog.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.blog.auth.dto.LoginRequest;
import com.example.blog.auth.dto.RegisterRequest;
import com.example.blog.config.AppProperties;
import com.example.blog.security.JwtService;
import com.example.blog.security.UserPrincipal;
import com.example.blog.user.domain.RoleEntity;
import com.example.blog.user.domain.RoleName;
import com.example.blog.user.domain.UserEntity;
import com.example.blog.user.repository.RoleRepository;
import com.example.blog.user.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("blog-platform", "01234567890123456789012345678901", 120),
                new AppProperties.Cache(false)
        );
        jwtService = new JwtService(properties) {
            @Override
            public String generateToken(UserPrincipal principal) {
                return "token-value";
            }
        };
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, authenticationManager, jwtService, properties);
    }

    @Test
    void registerCreatesAuthorAccount() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.AUTHOR)).thenReturn(Optional.of(new RoleEntity(RoleName.AUTHOR)));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new RegisterRequest("Alice", "alice@example.com", "password123"));

        assertThat(response.token()).isEqualTo("token-value");
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        assertThat(response.user().roles()).containsExactly(RoleName.AUTHOR);
    }

    @Test
    void loginReturnsJwtToken() {
        UserPrincipal principal = new UserPrincipal(1L, "Alice", "alice@example.com", "encoded", Set.of(new SimpleGrantedAuthority("AUTHOR")));
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        UserEntity user = new UserEntity("Alice", "alice@example.com", "encoded", Set.of(new RoleEntity(RoleName.AUTHOR)));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        var response = authService.login(new LoginRequest("alice@example.com", "password123"));

        assertThat(response.token()).isEqualTo("token-value");
        assertThat(response.user().name()).isEqualTo("Alice");
    }
}
