package ru.practicum.main.comment.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentSpecifications {

    public static Specification<Comment> publicSearch(Long eventId, String text, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            ps.add(cb.notEqual(root.get("status"), CommentStatus.DELETED));

            if (eventId != null) {
                ps.add(cb.equal(root.get("event").get("id"), eventId));
            }
            if (text != null && !text.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("text")), "%" + text.toLowerCase() + "%"));
            }
            if (rangeStart != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdOn"), rangeStart));
            }
            if (rangeEnd != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdOn"), rangeEnd));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    public static Specification<Comment> adminSearch(
            List<Long> authors, List<CommentStatus> states, List<Long> events,
            LocalDateTime rangeStart, LocalDateTime rangeEnd
    ) {
        return (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            if (authors != null && !authors.isEmpty()) {
                ps.add(root.get("author").get("id").in(authors));
            }
            if (states != null && !states.isEmpty()) {
                ps.add(root.get("status").in(states));
            }
            if (events != null && !events.isEmpty()) {
                ps.add(root.get("event").get("id").in(events));
            }
            if (rangeStart != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdOn"), rangeStart));
            }
            if (rangeEnd != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdOn"), rangeEnd));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
