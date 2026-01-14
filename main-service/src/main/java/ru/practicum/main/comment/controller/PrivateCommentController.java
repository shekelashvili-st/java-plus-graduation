package ru.practicum.main.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.service.CommentService;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @Valid @RequestBody NewCommentDto body) {
        log.info("PRIVATE create comment by user={} for event={}", userId, eventId);
        return commentService.createComment(userId, eventId, body);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @Positive Long eventId,
                                    @PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId,
                                    @Valid @RequestBody UpdateCommentDto body) {
        log.info("PRIVATE update comment={} by user={} to event={}", commentId, userId, eventId);
        return commentService.updateComment(eventId, userId, commentId, body);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long eventId,
                              @PathVariable @Positive Long userId,
                              @PathVariable @Positive Long commentId) {
        log.info("PRIVATE delete comment={} by user={} to event={}", commentId, userId, eventId);
        commentService.deleteComment(eventId, userId, commentId);
    }
}