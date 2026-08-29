package com.example.blog.security;

import com.example.blog.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppProperties appProperties;
    private final Key signingKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        byte[] secretBytes = appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 characters/bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(appProperties.jwt().expirationMinutes(), ChronoUnit.MINUTES);
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        Map<String, Object> claims = new HashMap<>();
        if (principal.getId() != null) {
            claims.put("uid", principal.getId());
        }
        if (principal.getName() != null) {
            claims.put("name", principal.getName());
        }
        claims.put("roles", roles);
        return Jwts.builder()
                .issuer(appProperties.jwt().issuer())
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(claims)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Claims extractClaims(String token) {
        return parseClaims(token);
    }

    public boolean isTokenValid(String token, UserPrincipal principal) {
        Claims claims = parseClaims(token);
        return claims.getSubject().equals(principal.getUsername()) && claims.getExpiration().after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
