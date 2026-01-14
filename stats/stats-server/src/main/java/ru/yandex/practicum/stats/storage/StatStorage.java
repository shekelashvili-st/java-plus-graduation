package ru.yandex.practicum.stats.storage;

import ru.yandex.practicum.stats.dto.EndpointHit;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatStorage {
    long saveHit(EndpointHit hit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
