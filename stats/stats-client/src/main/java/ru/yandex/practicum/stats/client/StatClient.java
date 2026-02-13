package ru.yandex.practicum.stats.client;

import java.util.Map;

public interface StatClient {
    void saveView(Long eventId, Long userId);

    void saveLike(Long eventId, Long userId);

    void saveRequest(Long eventId, Long userId);

    Map<Long, Double> fetchScore(Iterable<Long> ids);

    Map<Long, Double> fetchRecommendations(Long userId, Long maxResults);
}
