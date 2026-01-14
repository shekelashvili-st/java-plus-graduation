package ru.practicum.main.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.dto.UpdateCommentDto;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.comment.mapper.CommentMapper;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.comment.repository.CommentSpecifications;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.event.entity.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.ForbiddenException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.request.entity.RequestStatus;
import ru.practicum.main.request.repository.ParticipationRequestRepository;
import ru.practicum.main.user.entity.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;

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

        return CommentMapper.toDto(comment);
    }

    // ===== Private =====
    @Override
    @Transactional
    public CommentDto createComment(Long authorId, Long eventId, NewCommentDto body) {
        Event event = checkEvent(eventId);
        User author = checkAuthor(authorId);

        Comment comment = CommentMapper.toEntity(body, event, author);
        return createCommentInternal(author, event, comment, false);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long eventId, Long userId, Long commentId, UpdateCommentDto body) {
        Comment comment = checkCommentAndEvent(commentId, eventId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comment");
        }

        comment.setText(body.getText());
        comment.setStatus(CommentStatus.UPDATED);
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long eventId, Long userId, Long commentId) {
        Comment comment = checkCommentAndEvent(commentId, eventId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comment");
        }
        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    // ===== Admin =====
    @Override
    @Transactional
    public CommentDto createCommentByAdmin(Long authorId, Long eventId, NewCommentDto body) {
        Event event = checkEvent(eventId);
        User author = checkAuthor(authorId);

        Comment comment = CommentMapper.toEntity(body, event, author);
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
        return CommentMapper.toDto(commentRepository.save(comment));
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

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));
    }

    private Comment checkComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));
    }

    private User checkAuthor(Long authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User with id=" + authorId + " not found"));
    }

    private Comment checkCommentAndEvent(Long commentId, Long eventId) {
        Comment comment = checkComment(commentId);
        Event event = checkEvent(eventId);

        if (!comment.getEvent().getId().equals(event.getId())) {
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

        return commentRepository.findAll(spec, pageable).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    private CommentDto createCommentInternal(User author, Event event, Comment comment, boolean isAdminAction) {
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
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private boolean isUserParticipating(Long eventId, Long userId) {
        return participationRequestRepository.existsByEventIdAndRequesterIdAndStatus(eventId, userId, RequestStatus.CONFIRMED);
    }
}