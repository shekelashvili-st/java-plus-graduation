package ru.yandex.practicum.core.main.service.compilation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.main.service.compilation.dto.CompilationDto;
import ru.yandex.practicum.core.main.service.compilation.dto.NewCompilationDto;
import ru.yandex.practicum.core.main.service.compilation.dto.UpdateCompilationRequest;
import ru.yandex.practicum.core.main.service.compilation.service.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {
    private final CompilationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@RequestBody @Valid NewCompilationDto dto) {
        return service.create(dto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@RequestBody @Valid UpdateCompilationRequest dto,
                                 @PathVariable @Positive Long compId) {
        return service.update(dto, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compId) {
        service.delete(compId);
    }
}
