package ru.yandex.practicum.core.event.compilation.service;

import ru.yandex.practicum.core.common.dto.compilation.CompilationDto;
import ru.yandex.practicum.core.common.dto.compilation.FindCompilationParams;
import ru.yandex.practicum.core.common.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.core.common.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto dto);

    CompilationDto update(UpdateCompilationRequest dto, Long compId);

    void delete(Long compId);

    CompilationDto findById(Long compId);

    List<CompilationDto> getAll(FindCompilationParams params);
}
