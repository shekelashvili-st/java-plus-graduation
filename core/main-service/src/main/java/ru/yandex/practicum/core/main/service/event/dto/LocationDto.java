package ru.yandex.practicum.core.main.service.event.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    private Float lat;
    private Float lon;
}
