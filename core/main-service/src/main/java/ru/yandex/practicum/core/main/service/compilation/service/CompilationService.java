package ru.yandex.practicum.core.main.service.compilation.service;

import ru.yandex.practicum.core.main.service.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.main.service.compilation.dto.FindCompilationParams;
import ru.yandex.practicum.core.main.service.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.core.main.service.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto dto);

    CompilationDto update(UpdateCompilationRequest dto, Long compId);

    void delete(Long compId);

    CompilationDto findById(Long compId);

    List<CompilationDto> getAll(FindCompilationParams params);
}
