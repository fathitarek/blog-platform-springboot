package com.example.blog.post.specification;

import com.example.blog.post.domain.Category;
import com.example.blog.post.domain.PostEntity;
import com.example.blog.post.dto.PostFilterRequest;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class PostSpecification {

    private PostSpecification() {
    }

    public static Specification<PostEntity> fromFilter(PostFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch("author", JoinType.LEFT);
                query.distinct(true);
            }

            var predicate = criteriaBuilder.conjunction();

            if (filter.category() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category"), filter.category()));
            }

            if (filter.authorId() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("author").get("id"), filter.authorId()));
            }

            if (filter.fromDate() != null) {
                LocalDateTime from = filter.fromDate().atStartOfDay();
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (filter.toDate() != null) {
                LocalDateTime to = filter.toDate().atTime(23, 59, 59);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if (filter.search() != null && !filter.search().isBlank()) {
                String like = "%" + filter.search().trim().toLowerCase() + "%";
                var title = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), like);
                var authorName = criteriaBuilder.like(criteriaBuilder.lower(root.get("author").get("name")), like);
                var categoryMatch = Category.fromSearch(filter.search())
                        .<jakarta.persistence.criteria.Predicate>map(category -> criteriaBuilder.equal(root.get("category"), category))
                        .orElseGet(criteriaBuilder::disjunction);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(title, authorName, categoryMatch));
            }

            return predicate;
        };
    }
}
