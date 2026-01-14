package ru.yandex.practicum.core.main.service.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.core.main.service.comment.dto.CommentDto;
import ru.yandex.practicum.core.main.service.comment.dto.NewCommentDto;
import ru.yandex.practicum.core.main.service.comment.entity.Comment;
import ru.yandex.practicum.core.main.service.comment.entity.CommentStatus;
import ru.yandex.practicum.core.main.service.event.entity.Event;
import ru.yandex.practicum.core.main.service.user.dto.UserShortDto;
import ru.yandex.practicum.core.main.service.user.entity.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment toEntity(NewCommentDto dto, Event event, User author) {
        return Comment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .createdOn(LocalDateTime.now())
                .status(CommentStatus.CREATED)
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(new UserShortDto(comment.getAuthor().getId(), comment.getAuthor().getName()))
                .eventId(comment.getEvent().getId())
                .createdOn(comment.getCreatedOn())
                .status(comment.getStatus())
                .build();
    }
}
