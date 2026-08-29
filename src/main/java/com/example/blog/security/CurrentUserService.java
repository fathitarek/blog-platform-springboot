package com.example.blog.security;

import com.example.blog.common.exception.ForbiddenOperationException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenOperationException("Authentication is required");
        }
        return principal;
    }

    public Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public boolean isAdmin() {
        Set<String> authorities = getCurrentPrincipal().getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());
        return authorities.contains("ADMIN");
    }
}
