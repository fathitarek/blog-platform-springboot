package com.example.blog.post.dto;

import com.example.blog.post.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostUpsertRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @NotNull Category category
) {
}
