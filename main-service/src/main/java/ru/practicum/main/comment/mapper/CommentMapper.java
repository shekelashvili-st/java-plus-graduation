package ru.practicum.main.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.NewCommentDto;
import ru.practicum.main.comment.entity.Comment;
import ru.practicum.main.comment.entity.CommentStatus;
import ru.practicum.main.event.entity.Event;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.entity.User;

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
