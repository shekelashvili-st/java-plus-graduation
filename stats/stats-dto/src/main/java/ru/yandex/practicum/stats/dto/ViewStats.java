package ru.yandex.practicum.stats.dto;

import lombok.Data;

@Data
public class ViewStats {
    private final String app;
    private final String uri;
    private final int hits;
}
