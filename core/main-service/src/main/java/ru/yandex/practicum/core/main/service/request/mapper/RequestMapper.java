package ru.yandex.practicum.core.main.service.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.core.common.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.core.main.service.request.entity.ParticipationRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest e) {
        return ParticipationRequestDto.builder()
                .id(e.getId())
                .created(e.getCreated())
                .event(e.getEventId())
                .requester(e.getRequesterId())
                .status(ru.yandex.practicum.core.common.dto.request.RequestStatus.valueOf(e.getStatus().toString()))
                .build();
    }
}
