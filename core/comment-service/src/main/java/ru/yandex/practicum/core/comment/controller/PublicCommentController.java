package ru.yandex.practicum.core.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.comment.service.CommentService;
import ru.yandex.practicum.core.common.dto.comment.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllComments(@PathVariable @Positive Long eventId,
                                           @RequestParam(required = false) String text,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                           @RequestParam(defaultValue = "10") @Positive Integer size) {
        return commentService.getCommentsByEvent(eventId, text, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable @Positive Long eventId,
                                 @PathVariable @Positive Long commentId) {
        return commentService.getCommentById(commentId, eventId);
    }
}