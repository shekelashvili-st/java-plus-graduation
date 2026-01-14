package ru.practicum.main.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.service.CommentService;

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
        log.info("PUBLIC get comments for event={}", eventId);
        return commentService.getCommentsByEvent(eventId, text, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable @Positive Long eventId,
                                 @PathVariable @Positive Long commentId) {
        log.info("PUBLIC get comment={} to event={}", commentId, eventId);
        return commentService.getCommentById(commentId, eventId);
    }
}