package ru.practicum.main.comment.service;

import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    // Public
    List<CommentDto> getCommentsByEvent(Long eventId, String text, String rangeStart, String rangeEnd, Integer from, Integer size);

    CommentDto getCommentById(Long commentId, Long eventId);

    // Private
    CommentDto createComment(Long userId, Long eventId, NewCommentDto body);

    CommentDto updateComment(Long eventId, Long userId, Long commentId, UpdateCommentDto body);

    void deleteComment(Long eventId, Long userId, Long commentId);

    // Admin
    CommentDto createCommentByAdmin(Long authorId, Long eventId, NewCommentDto body);

    List<CommentDto> searchCommentByAdmin(List<Long> authors, List<String> states, List<Long> events,
                                          String rangeStart, String rangeEnd, Integer from, Integer size);

    CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto body);

    void deleteCommentByAdmin(Long commentId);
}
