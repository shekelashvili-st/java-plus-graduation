package ru.yandex.practicum.core.common.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class UserShortDto {
    @EqualsAndHashCode.Include
    private long id;
    @NotBlank
    private final String name;
}
