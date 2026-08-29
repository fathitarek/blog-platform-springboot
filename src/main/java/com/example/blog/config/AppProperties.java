package com.example.blog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cache cache) {

    public record Jwt(
            @NotBlank String issuer,
            @NotBlank String secret,
            @Min(1) long expirationMinutes
    ) {
    }

    public record Cache(boolean redisEnabled) {
    }
}
