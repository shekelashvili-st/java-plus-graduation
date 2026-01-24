package ru.yandex.practicum.core.event.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.common.dto.event.EventInternalDto;
import ru.yandex.practicum.core.event.event.service.EventService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public EventInternalDto getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }
}

