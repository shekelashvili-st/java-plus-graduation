package ru.yandex.practicum.core.main.service.request.service;

import ru.yandex.practicum.core.main.service.request.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.main.service.request.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.main.service.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByUserId(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest body);
}
