package ru.yandex.practicum.core.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.comment.entity.Comment;
import ru.yandex.practicum.core.comment.entity.CommentStatus;
import ru.yandex.practicum.core.comment.mapper.CommentMapper;
import ru.yandex.practicum.core.comment.repository.CommentRepository;
import ru.yandex.practicum.core.comment.repository.CommentSpecifications;
import ru.yandex.practicum.core.common.client.event.EventClient;
import ru.yandex.practicum.core.common.client.request.RequestClient;
import ru.yandex.practicum.core.common.client.user.UserClient;
import ru.yandex.practicum.core.common.dto.comment.CommentDto;
import ru.yandex.practicum.core.common.dto.comment.NewCommentDto;
import ru.yandex.practicum.core.common.dto.comment.UpdateCommentDto;
import ru.yandex.practicum.core.common.dto.event.EventInternalDto;
import ru.yandex.practicum.core.common.dto.event.EventState;
import ru.yandex.practicum.core.common.dto.request.RequestStatus;
import ru.yandex.practicum.core.common.dto.user.UserDto;
import ru.yandex.practicum.core.common.exception.BadRequestException;
import ru.yandex.practicum.core.common.exception.ConflictException;
import ru.yandex.practicum.core.common.exception.ForbiddenException;
import ru.yandex.practicum.core.common.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CommentRepository commentRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    // ===== Public =====
    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, String text, String rangeStart, String rangeEnd,
                                               Integer from, Integer size) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, F) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, F) : null;
        checkTimeRange(start, end);

        var spec = CommentSpecifications.publicSearch(eventId, text, start, end);

        return getPaginatedComments(spec, from, size);
    }

    @Override
    public CommentDto getCommentById(Long commentId, Long eventId) {
        Comment comment = checkCommentAndEvent(commentId, eventId);

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new NotFoundException("Comment with id=" + commentId + " was deleted");
        }
        String authorName = userClient.getUserById(comment.getAuthorId()).getName();

        return CommentMapper.toDto(comment, authorName);
    }

    // ===== Private =====
    @Override
    @Transactional
    public CommentDto createComment(Long authorId, Long eventId, NewCommentDto body) {
        EventInternalDto event = checkEvent(eventId);
        UserDto author = checkAuthor(authorId);

        Comment comment = CommentMapper.toEntity(body, eventId, author.getId());
        return createCommentInternal(author, event, comment, false);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long eventId, Long userId, Long commentId, UpdateCommentDto body) {
        Comment comment = checkCommentAndEvent(commentId, eventId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comment");
        }

        comment.setText(body.getText());
        comment.setStatus(CommentStatus.UPDATED);
        return CommentMapper.toDto(commentRepository.save(comment), userClient.getUserById(userId).getName());
    }

    @Override
    @Transactional
    public void deleteComment(Long eventId, Long userId, Long commentId) {
        Comment comment = checkCommentAndEvent(commentId, eventId);

        if (!comment.getAuthorId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comment");
        }
        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    // ===== Admin =====
    @Override
    @Transactional
    public CommentDto createCommentByAdmin(Long authorId, Long eventId, NewCommentDto body) {
        EventInternalDto event = checkEvent(eventId);
        UserDto author = checkAuthor(authorId);

        Comment comment = CommentMapper.toEntity(body, eventId, author.getId());
        return createCommentInternal(author, event, comment, true);
    }

    @Override
    public List<CommentDto> searchCommentByAdmin(List<Long> authors, List<String> statesStr, List<Long> events,
                                                 String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, F) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, F) : null;
        checkTimeRange(start, end);

        List<CommentStatus> states = null;
        if (statesStr != null && !statesStr.isEmpty()) {
            states = statesStr.stream()
                    .map(s -> {
                        try {
                            return CommentStatus.valueOf(s.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException("Invalid status: " + s);
                        }
                    })
                    .collect(Collectors.toList());
        }

        var spec = CommentSpecifications.adminSearch(authors, states, events, start, end);

        return getPaginatedComments(spec, from, size);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(Long commentId, UpdateCommentDto body) {
        Comment comment = checkComment(commentId);
        comment.setText(body.getText());
        comment.setStatus(CommentStatus.UPDATED);
        return CommentMapper.toDto(commentRepository.save(comment), userClient.getUserById(comment.getAuthorId()).getName());
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = checkComment(commentId);
        commentRepository.deleteById(comment.getId());
    }

    // ===== Helpers =====
    private void checkTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }
    }

    private EventInternalDto checkEvent(Long eventId) {
        return eventClient.getEventById(eventId);
    }

    private Comment checkComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));
    }

    private UserDto checkAuthor(Long authorId) {
        return userClient.getUserById(authorId);
    }

    private Comment checkCommentAndEvent(Long commentId, Long eventId) {
        Comment comment = checkComment(commentId);
        EventInternalDto event = checkEvent(eventId);

        if (!comment.getEventId().equals(event.getId())) {
            throw new BadRequestException("Comment with id=" + commentId
                    + " does not belong to event with id=" + eventId);
        }
        return comment;
    }

    private List<CommentDto> getPaginatedComments(org.springframework.data.jpa.domain.Specification<Comment> spec,
                                                  Integer from, Integer size) {
        int f = from != null ? from : 0;
        int s = size != null ? size : 10;
        Pageable pageable = PageRequest.of(f / s, s, Sort.by(Sort.Direction.DESC, "createdOn"));

        List<Comment> comments = commentRepository.findAll(spec, pageable).getContent();
        Map<Long, UserDto> commentToAuthor = userClient.getUsersById(comments.stream().map(Comment::getAuthorId).toList())
                .stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        return commentRepository.findAll(spec, pageable).stream()
                .map(comment -> CommentMapper.toDto(comment, commentToAuthor.get(comment.getAuthorId()).getName()))
                .collect(Collectors.toList());
    }

    private CommentDto createCommentInternal(UserDto author, EventInternalDto event, Comment comment, boolean isAdminAction) {
        if (!isAdminAction) {
            if (event.getState() != EventState.PUBLISHED && event.getState() != EventState.CANCELED) {
                throw new ConflictException("Only PUBLISHED or CANCELED events can be commented on by users");
            }
            if (event.getState() != EventState.CANCELED && !isUserParticipating(event.getId(), author.getId())) {
                throw new ForbiddenException("User is not participating in event he tries to comment");
            }
        }

        if (isAdminAction) {
            log.info("Admin created comment for user {} on event {}", author.getId(), event.getId());
        }
        return CommentMapper.toDto(commentRepository.save(comment), author.getName());
    }

    private boolean isUserParticipating(Long eventId, Long userId) {
        return requestClient.existsByEventIdAndRequesterIdAndStatus(eventId, userId, RequestStatus.CONFIRMED);
    }
}