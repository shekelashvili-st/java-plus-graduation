package ru.yandex.practicum.core.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.common.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.common.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.common.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.core.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventRequestsController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequestsByUserId(@PathVariable Long userId,
                                                                  @PathVariable Long eventId) {
        List<ParticipationRequestDto> newList = requestService.getEventRequestsByUserId(userId, eventId);
        return newList;
    }

    @PatchMapping
    public EventRequestStatusUpdateResult changeStatus(@PathVariable Long userId,
                                                       @PathVariable Long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest body) {
        return requestService.changeRequestStatus(userId, eventId, body);
    }
}
