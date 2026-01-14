package ru.yandex.practicum.core.main.service.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.yandex.practicum.core.main.service.request.entity.ParticipationStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ParticipationStatus status;
}
