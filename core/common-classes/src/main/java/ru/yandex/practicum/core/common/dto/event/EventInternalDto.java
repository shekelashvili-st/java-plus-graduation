package ru.yandex.practicum.core.common.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.yandex.practicum.core.common.dto.user.UserShortDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventInternalDto {
    private Long id;

    private UserShortDto initiator;

    @Builder.Default
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventState state;
}
