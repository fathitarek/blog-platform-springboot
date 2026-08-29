package com.example.blog.post.dto;

import com.example.blog.post.domain.Category;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;

public record PostFilterRequest(
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection,
        Category category,
        Long authorId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        String search
) {
    public int resolvedPage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int resolvedSize() {
        return size == null || size < 1 ? 20 : Math.min(size, 100);
    }

    public String resolvedSortBy() {
        return (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    }

    public Sort.Direction resolvedSortDirection() {
        return sortDirection == null || sortDirection.isBlank()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(sortDirection.trim().toUpperCase());
    }

    public String cacheKey() {
        return String.join(":",
                String.valueOf(resolvedPage()),
                String.valueOf(resolvedSize()),
                resolvedSortBy(),
                resolvedSortDirection().name(),
                category == null ? "any" : category.name(),
                authorId == null ? "any" : String.valueOf(authorId),
                fromDate == null ? "any" : fromDate.toString(),
                toDate == null ? "any" : toDate.toString(),
                search == null ? "any" : search.trim().toLowerCase()
        );
    }
}
