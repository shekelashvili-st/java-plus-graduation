package ru.practicum.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllPublicEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @Pattern(regexp = "EVENT_DATE|VIEWS", message = "sort must be EVENT_DATE or VIEWS")
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        log.info("PUBLIC /events from={} size={} ip={} uri={}", from, size, request.getRemoteAddr(), request.getRequestURI());
        List<EventShortDto> newList = eventService.getAllPublicEvents(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
        log.debug("SUCCESS: PUBLIC /events newList={}", newList);
        return newList;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getPublishedEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("PUBLIC /events/{}", eventId);
        EventFullDto newdto = eventService.getPublishedEventById(eventId, request);
        log.debug("SUCCESS: PUBLIC /events/{} newdto={} ip={} uri={}", eventId, newdto, request.getRemoteAddr(), request.getRequestURI());
        return newdto;
    }
}

