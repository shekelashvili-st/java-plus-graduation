package ru.yandex.practicum.core.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.comment.service.CommentService;
import ru.yandex.practicum.core.common.dto.comment.CommentDto;
import ru.yandex.practicum.core.common.dto.comment.NewCommentDto;
import ru.yandex.practicum.core.common.dto.comment.UpdateCommentDto;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createByAdmin(@PathVariable @Positive Long eventId,
                                    @RequestParam @Positive Long authorId,
                                    @Valid @RequestBody NewCommentDto body) {
        return commentService.createCommentByAdmin(authorId, eventId, body);
    }

    @GetMapping
    public List<CommentDto> searchByAdmin(@RequestParam(required = false) List<Long> authors,
                                          @RequestParam(required = false) List<String> states,
                                          @RequestParam(required = false) List<Long> events,
                                          @RequestParam(required = false) String rangeStart,
                                          @RequestParam(required = false) String rangeEnd,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                          @RequestParam(defaultValue = "10") @Positive Integer size) {
        return commentService.searchCommentByAdmin(authors, states, events, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateByAdmin(@PathVariable @Positive Long commentId,
                                    @Valid @RequestBody UpdateCommentDto body) {
        return commentService.updateCommentByAdmin(commentId, body);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable @Positive Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}