package ru.yandex.practicum.core.common.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

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
