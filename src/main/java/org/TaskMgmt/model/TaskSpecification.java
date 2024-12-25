package org.TaskMgmt.model;

import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class TaskSpecification {
    public static Specification<Task> filterTasks(String title, String status, LocalDateTime deadlineStart, LocalDateTime deadlineEnd, String priority, String category) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (title != null && !title.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("status"), status));
            }
            if (deadlineStart != null) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), deadlineStart));
            }
            if (deadlineEnd != null) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.lessThanOrEqualTo(root.get("deadline"), deadlineEnd));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (category != null && !category.isEmpty()) {
                predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("category"), category));
            }
            return predicates;
        };
    }
}
