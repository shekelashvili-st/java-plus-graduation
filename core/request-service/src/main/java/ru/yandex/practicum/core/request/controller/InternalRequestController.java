package ru.yandex.practicum.core.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.common.dto.request.RequestStatus;
import ru.yandex.practicum.core.request.service.RequestService;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {
    private final RequestService requestService;

    @GetMapping("/exists")
    public boolean existsByEventIdAndRequesterIdAndStatus(@RequestParam Long eventId,
                                                          @RequestParam Long userId,
                                                          @RequestParam RequestStatus status) {
        return requestService.existsByEventIdAndRequesterIdAndStatus(eventId, userId,
                ru.yandex.practicum.core.request.entity.RequestStatus.valueOf(status.toString()));
    }

    @GetMapping("/count")
    public long countByEventIdAndStatus(@RequestParam Long eventId,
                                        @RequestParam RequestStatus status) {
        return requestService.countByEventIdAndStatus(eventId,
                ru.yandex.practicum.core.request.entity.RequestStatus.valueOf(status.toString()));
    }
}
