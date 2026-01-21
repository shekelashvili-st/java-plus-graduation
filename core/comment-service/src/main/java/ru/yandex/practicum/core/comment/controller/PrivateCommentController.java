package ru.yandex.practicum.core.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.comment.service.CommentService;
import ru.yandex.practicum.core.common.dto.comment.CommentDto;
import ru.yandex.practicum.core.common.dto.comment.NewCommentDto;
import ru.yandex.practicum.core.common.dto.comment.UpdateCommentDto;

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
        return commentService.createComment(userId, eventId, body);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable @Positive Long eventId,
                                    @PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long commentId,
                                    @Valid @RequestBody UpdateCommentDto body) {
        return commentService.updateComment(eventId, userId, commentId, body);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long eventId,
                              @PathVariable @Positive Long userId,
                              @PathVariable @Positive Long commentId) {
        commentService.deleteComment(eventId, userId, commentId);
    }
}