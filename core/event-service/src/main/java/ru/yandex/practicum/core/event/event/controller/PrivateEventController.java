package ru.yandex.practicum.core.event.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.common.dto.event.EventFullDto;
import ru.yandex.practicum.core.common.dto.event.EventShortDto;
import ru.yandex.practicum.core.common.dto.event.NewEventDto;
import ru.yandex.practicum.core.common.dto.event.UpdateEventUserRequest;
import ru.yandex.practicum.core.event.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        List<EventShortDto> newList = eventService.getUserEvents(userId, from, size);
        return newList;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto body) {
        EventFullDto newDto = eventService.addEvent(userId, body);
        return newDto;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        EventFullDto newDto = eventService.getEventByUser(userId, eventId);
        return newDto;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest body) {
        EventFullDto newDto = eventService.updateUserEvent(userId, eventId, body);
        return newDto;
    }
}

