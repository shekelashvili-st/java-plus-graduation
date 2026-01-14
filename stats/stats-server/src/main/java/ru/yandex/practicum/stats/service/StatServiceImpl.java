package ru.yandex.practicum.stats.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.stats.dto.EndpointHit;
import ru.yandex.practicum.stats.dto.ViewStats;
import ru.yandex.practicum.stats.exception.BadRequestException;
import ru.yandex.practicum.stats.storage.StatStorage;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatStorage statStorage;

    @Override
    public void hit(EndpointHit hit) {
        statStorage.saveHit(hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        return statStorage.getStats(start, end, uris, unique);
    }
}
