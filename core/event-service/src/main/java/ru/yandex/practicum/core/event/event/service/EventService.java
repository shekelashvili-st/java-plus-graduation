package ru.yandex.practicum.core.event.event.service;

import ru.yandex.practicum.core.common.dto.event.*;

import java.util.List;

public interface EventService {
    List<EventShortDto> getRecommendations(Long userId, Long maxResults);

    void likeEvent(Long userId, Long eventId);

    // ===== Public =====
    List<EventShortDto> getAllPublicEvents(String text, List<Long> categories,
                                           Boolean paid, String rangeStart, String rangeEnd,
                                           Boolean onlyAvailable, String sort, Integer from, Integer size);

    EventFullDto getPublishedEventById(Long eventId, Long userId);

    // ===== Private =====
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto body);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body);

    // ===== Admin =====
    List<EventFullDto> searchAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest body);

    // ==== Internal ====
    EventInternalDto getEventById(Long id);
}

