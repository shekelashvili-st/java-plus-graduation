package ru.yandex.practicum.core.main.service.compilation.dto;

import lombok.Value;

@Value
public class FindCompilationParams {
    Boolean pinned;
    Integer from;
    Integer size;
}
