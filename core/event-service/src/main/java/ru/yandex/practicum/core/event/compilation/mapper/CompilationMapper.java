package ru.yandex.practicum.core.event.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.core.common.dto.compilation.CompilationDto;
import ru.yandex.practicum.core.common.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.core.event.compilation.entity.Compilation;
import ru.yandex.practicum.core.event.event.mapper.EventMapper;

@UtilityClass
public class CompilationMapper {
    public static Compilation toEntity(NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }
        return new Compilation(null, dto.getTitle(), dto.isPinned(), null, null);
    }

    public static CompilationDto toDto(Compilation entity) {
        if (entity == null) {
            return null;
        }
        return new CompilationDto(entity.getId(),
                entity.isPinned(),
                entity.getTitle(),
                entity.getEvents().stream().map(EventMapper::toShortDto).toList());
    }
}
