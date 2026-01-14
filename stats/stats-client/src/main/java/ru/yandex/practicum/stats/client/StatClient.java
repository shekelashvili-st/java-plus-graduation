package ru.yandex.practicum.stats.client;

import ru.yandex.practicum.stats.dto.EndpointHit;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatClient {
    void create(EndpointHit endpointHit);

    List<ViewStats> getStats(LocalDateTime start,
                             LocalDateTime end,
                             List<String> uris,
                             boolean unique);
}
