package ru.yandex.practicum.core.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.core.comment.entity.Comment;
import ru.yandex.practicum.core.comment.entity.CommentStatus;
import ru.yandex.practicum.core.common.dto.comment.CommentDto;
import ru.yandex.practicum.core.common.dto.comment.NewCommentDto;
import ru.yandex.practicum.core.common.dto.user.UserShortDto;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toEntity(NewCommentDto dto, Long eventId, Long authorId) {
        return Comment.builder()
                .text(dto.getText())
                .eventId(eventId)
                .authorId(authorId)
                .createdOn(LocalDateTime.now())
                .status(CommentStatus.CREATED)
                .build();
    }

    public static CommentDto toDto(Comment comment, String authorName) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(new UserShortDto(comment.getAuthorId(), authorName))
                .eventId(comment.getEventId())
                .createdOn(comment.getCreatedOn())
                .status(ru.yandex.practicum.core.common.dto.comment.CommentStatus.valueOf(comment.getStatus().toString()))
                .build();
    }
}
