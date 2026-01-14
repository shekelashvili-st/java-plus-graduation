package ru.yandex.practicum.core.main.service.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.core.main.service.event.dto.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
public class CompilationDto {
    private Long id;
    private boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
