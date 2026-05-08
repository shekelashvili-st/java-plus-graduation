package ru.yandex.practicum.core.event.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.common.client.request.RequestClient;
import ru.yandex.practicum.core.common.dto.category.CategoryDto;
import ru.yandex.practicum.core.common.dto.event.*;
import ru.yandex.practicum.core.common.dto.request.RequestStatus;
import ru.yandex.practicum.core.common.dto.user.UserShortDto;
import ru.yandex.practicum.core.common.exception.BadRequestException;
import ru.yandex.practicum.core.common.exception.ConflictException;
import ru.yandex.practicum.core.common.exception.NotFoundException;
import ru.yandex.practicum.core.event.category.entity.Category;
import ru.yandex.practicum.core.event.category.mapper.CategoryMapper;
import ru.yandex.practicum.core.event.category.repository.CategoryRepository;
import ru.yandex.practicum.core.event.event.entity.Event;
import ru.yandex.practicum.core.event.event.entity.EventState;
import ru.yandex.practicum.core.event.event.mapper.EventMapper;
import ru.yandex.practicum.core.event.event.repository.EventRepository;
import ru.yandex.practicum.core.event.event.repository.EventSpecifications;
import ru.yandex.practicum.stats.client.StatClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestClient requestClient;
    private final StatClient statClient;

    // ===== Public =====

    @Override
    public List<EventShortDto> getRecommendations(Long userId, Long maxResults) {
        Map<Long, Double> recommendedIdsToScore = statClient.fetchRecommendations(userId, maxResults);
        List<Event> events = eventRepository.findAllById(recommendedIdsToScore.keySet());

        Map<Long, Double> ratingByEvent = fetchRating(events);

        return events.stream()
                .map(e -> toShortDtoRich(e, ratingByEvent.getOrDefault(e.getId(), 0D)))
                .collect(Collectors.toList());
    }

    @Override
    public void likeEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        boolean hasRequest = requestClient.existsByEventIdAndRequesterIdAndStatus(eventId, userId, RequestStatus.CONFIRMED);
        if (!((event.getState() == EventState.PUBLISHED)
                && hasRequest
                && event.getEventDate().isBefore(LocalDateTime.now()))) {
            throw new BadRequestException("User hasn't participated in the event");
        }
        statClient.saveLike(eventId, userId);
    }

    @Override
    public List<EventShortDto> getAllPublicEvents(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  String rangeStart,
                                                  String rangeEnd,
                                                  Boolean onlyAvailable,
                                                  String sort,
                                                  Integer from,
                                                  Integer size) {

        LocalDateTime start = parse(rangeStart);
        LocalDateTime end = parse(rangeEnd);

        if (start == null && end == null) start = LocalDateTime.now();
        ensureRangeValid(start, end);

        var spec = EventSpecifications.publicSearch(text, categories, paid, start, end);

        List<Event> events = eventRepository.findAll(spec);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(e -> {
                        Integer limit = e.getParticipantLimit();
                        if (limit == null || limit == 0) return true;
                        long confirmed = getConfirmedCount(e.getId());
                        return confirmed < limit;
                    })
                    .collect(Collectors.toList());
        }

        Map<Long, Double> ratingByEvent = fetchRating(events);

        String sortMode = (sort == null) ? "EVENT_DATE" : sort.toUpperCase(Locale.ROOT);
        Comparator<Event> byDate = Comparator.comparing(Event::getEventDate);
        if ("VIEWS".equals(sortMode)) {
            Comparator<Event> byViewsDesc = Comparator
                    .comparing((Event e) -> ratingByEvent.getOrDefault(e.getId(), 0D))
                    .reversed()
                    .thenComparing(byDate);
            events.sort(byViewsDesc);
        } else {
            events.sort(byDate);
        }

        int f = from == null ? 0 : Math.max(0, from);
        int s = size == null ? 10 : Math.max(1, size);
        int to = Math.min(events.size(), f + s);
        if (f >= events.size()) return List.of();

        return events.subList(f, to).stream()
                .map(e -> toShortDtoRich(e, ratingByEvent.getOrDefault(e.getId(), 0D)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublishedEventById(Long eventId, Long userId) {
        Event e = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        saveView(eventId, userId);

        Map<Long, Double> rating = fetchRating(List.of(e));
        return toFullDtoRich(e, rating.getOrDefault(e.getId(), 0D));
    }

    // ===== Private =====
    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        int f = from == null ? 0 : Math.max(0, from);
        int s = size == null ? 10 : Math.max(1, size);
        int page = f / s;

        Pageable pageable = PageRequest.of(page, s, Sort.by(Sort.Direction.DESC, "createdOn"));
        Page<Event> pageObj = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> events = pageObj.getContent();

        Map<Long, Double> ratingByEvent = fetchRating(events);

        return events.stream()
                .map(e -> toShortDtoRich(e, ratingByEvent.getOrDefault(e.getId(), 0D)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto body) {
        LocalDateTime eventDate = parseRequired(body.getEventDate());
        if (!eventDate.isAfter(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая ещё не наступила. Value: " + body.getEventDate());
        }

        Category category = categoryRepository.findById(body.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + body.getCategory() + " was not found"));

        Event preSaved = EventMapper.toEntity(body, category, userId);

        return toFullDtoRich(eventRepository.save(preSaved), 0D);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event e = getUserEventOrThrow(userId, eventId);
        Map<Long, Double> rating = fetchRating(List.of(e));
        return toFullDtoRich(e, rating.getOrDefault(e.getId(), 0D));
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        Event e = getUserEventOrThrow(userId, eventId);

        if (!(e.getState() == EventState.PENDING || e.getState() == EventState.CANCELED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (body.getAnnotation() != null) e.setAnnotation(body.getAnnotation());
        if (body.getDescription() != null) e.setDescription(body.getDescription());
        if (body.getTitle() != null) e.setTitle(body.getTitle());

        if (body.getEventDate() != null) {
            LocalDateTime newDate = body.getEventDate();
            if (!newDate.isAfter(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая ещё не наступила. Value: " + body.getEventDate());
            }
            e.setEventDate(newDate);
        }

        if (body.getLocation() != null) {
            e.setLat(body.getLocation().getLat());
            e.setLon(body.getLocation().getLon());
        }
        if (body.getPaid() != null) e.setPaid(body.getPaid());
        if (body.getParticipantLimit() != null) e.setParticipantLimit(body.getParticipantLimit());
        if (body.getRequestModeration() != null) e.setRequestModeration(body.getRequestModeration());
        if (body.getCategory() != null) {
            Category category = categoryRepository.findById(body.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + body.getCategory() + " was not found"));
            e.setCategory(category);
        }

        if (body.getStateAction() != null) {
            switch (body.getStateAction()) {
                case SEND_TO_REVIEW -> e.setState(EventState.PENDING);
                case CANCEL_REVIEW -> e.setState(EventState.CANCELED);
                default -> throw new ConflictException("Unknown stateAction: " + body.getStateAction());
            }
        }

        Event saved = eventRepository.save(e);
        Map<Long, Double> rating = fetchRating(List.of(saved));
        return toFullDtoRich(saved, rating.getOrDefault(saved.getId(), 0D));
    }

    // ===== Admin =====
    @Override
    public List<EventFullDto> searchAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                                String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = parse(rangeStart);
        LocalDateTime end = parse(rangeEnd);
        ensureRangeValid(start, end);

        List<EventState> st = null;
        if (states != null && !states.isEmpty()) {
            st = states.stream().map(s -> {
                try {
                    return EventState.valueOf(s);
                } catch (IllegalArgumentException ex) {
                    throw new ConflictException("Unknown state: " + s);
                }
            }).toList();
        }

        var spec = EventSpecifications.adminSearch(users, st, categories, start, end);
        List<Event> events = eventRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdOn"));

        int f = from == null ? 0 : Math.max(0, from);
        int s = size == null ? 10 : Math.max(1, size);
        int to = Math.min(events.size(), f + s);
        if (f >= events.size()) return List.of();

        Map<Long, Double> ratingByEvent = fetchRating(events.subList(f, to));

        return events.subList(f, to).stream()
                .map(e -> toFullDtoRich(e, ratingByEvent.getOrDefault(e.getId(), 0D)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest body) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (body.getAnnotation() != null) e.setAnnotation(body.getAnnotation());
        if (body.getDescription() != null) e.setDescription(body.getDescription());
        if (body.getTitle() != null) e.setTitle(body.getTitle());

        if (body.getEventDate() != null && LocalDateTime.now().isAfter(body.getEventDate())) {
            throw new BadRequestException("Date should be in the future. Value: " + body.getEventDate());
        }

        if (body.getEventDate() != null) e.setEventDate(body.getEventDate());
        if (body.getLocation() != null) {
            e.setLat(body.getLocation().getLat());
            e.setLon(body.getLocation().getLon());
        }
        if (body.getPaid() != null) e.setPaid(body.getPaid());
        if (body.getParticipantLimit() != null) e.setParticipantLimit(body.getParticipantLimit());
        if (body.getRequestModeration() != null) e.setRequestModeration(body.getRequestModeration());
        if (body.getCategory() != null) {
            Category category = categoryRepository.findById(body.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + body.getCategory() + " was not found"));
            e.setCategory(category);
        }

        if (body.getStateAction() != null) {
            switch (body.getStateAction().toString()) {
                case "PUBLISH_EVENT" -> {
                    if (e.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + e.getState());
                    }
                    LocalDateTime publishTime = LocalDateTime.now();
                    if (!e.getEventDate().isAfter(publishTime.plusHours(1))) {
                        throw new ConflictException("EventDate must be at least 1 hour after publication");
                    }
                    e.setPublishedOn(publishTime);
                    e.setState(EventState.PUBLISHED);
                }
                case "REJECT_EVENT" -> {
                    if (e.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's already published");
                    }
                    e.setState(EventState.CANCELED);
                }
                default -> throw new ConflictException("Unknown stateAction: " + body.getStateAction());
            }
        }

        Event saved = eventRepository.save(e);
        Map<Long, Double> rating = fetchRating(List.of(saved));
        return toFullDtoRich(saved, rating.getOrDefault(saved.getId(), 0D));
    }

    // ==== Internal ====
    @Override
    @Transactional(readOnly = true)
    public EventInternalDto getEventById(Long id) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
        return EventMapper.toInternalDto(e);
    }


    // ===== Helpers =====
    private void ensureRangeValid(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }
    }

    private LocalDateTime parse(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s, F);
    }

    private LocalDateTime parseRequired(String s) {
        return LocalDateTime.parse(s, F);
    }

    private Event getUserEventOrThrow(Long userId, Long eventId) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (!Objects.equals(e.getInitiatorId(), userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return e;
    }

    private long getConfirmedCount(Long eventId) {
        return requestClient.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private EventFullDto toFullDtoRich(Event e, Double rating) {
        EventFullDto dto = EventMapper.toFullDto(e);
        CategoryDto cat = CategoryMapper.toDto(e.getCategory());
        dto.setCategory(cat);
        dto.setInitiator(buildInitiator(e.getInitiatorId()));
        dto.setConfirmedRequests(getConfirmedCount(e.getId()));
        dto.setRating(rating);
        return dto;
    }

    private EventShortDto toShortDtoRich(Event e, Double rating) {
        EventShortDto dto = EventMapper.toShortDto(e);
        dto.setCategory(CategoryMapper.toDto(e.getCategory()));
        dto.setInitiator(buildInitiator(e.getInitiatorId()));
        dto.setConfirmedRequests(getConfirmedCount(e.getId()));
        dto.setRating(rating);
        return dto;
    }

    private UserShortDto buildInitiator(Long userId) {
        return new UserShortDto(userId, "");
    }

    // ===== Stats =====
    private void saveView(Long eventId, Long userId) {
        try {
            statClient.saveView(eventId, userId);
        } catch (Exception ex) {
            log.warn("StatService save view failed: {}", ex.getMessage());
        }
    }

    private Map<Long, Double> fetchRating(List<Event> events) {
        if (events == null || events.isEmpty()) return Map.of();
        try {
            return statClient.fetchScore(events.stream().map(Event::getId).toList());
        } catch (Exception ex) {
            log.warn("StatService fetchRating failed: {}", ex.getMessage());
            return events.stream().collect(Collectors.toMap(Event::getId, e -> 0D));
        }
    }
}
