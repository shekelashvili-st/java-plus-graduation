package ru.yandex.practicum.core.request.service;

import ru.yandex.practicum.core.common.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.common.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.common.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.core.request.entity.RequestStatus;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequestsByUserId(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest body);

    boolean existsByEventIdAndRequesterIdAndStatus(Long eventId, Long userId, RequestStatus requestStatus);

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);
}
