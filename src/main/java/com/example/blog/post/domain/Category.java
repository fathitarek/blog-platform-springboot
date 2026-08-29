package com.example.blog.post.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Category {
    TECHNOLOGY,
    LIFESTYLE,
    EDUCATION;

    public static Optional<Category> fromSearch(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
