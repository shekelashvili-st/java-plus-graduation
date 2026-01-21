package ru.yandex.practicum.core.common.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.core.common.dto.event.EventInternalDto;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {
    @GetMapping("/{id}")
    EventInternalDto getEventById(@PathVariable Long id);
}
