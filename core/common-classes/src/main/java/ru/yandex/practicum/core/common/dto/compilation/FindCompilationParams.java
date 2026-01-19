package ru.yandex.practicum.core.common.dto.compilation;

import lombok.Value;

@Value
public class FindCompilationParams {
    Boolean pinned;
    Integer from;
    Integer size;
}
