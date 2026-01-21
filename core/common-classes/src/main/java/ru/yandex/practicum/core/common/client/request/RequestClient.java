package ru.yandex.practicum.core.common.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.core.common.dto.request.RequestStatus;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {
    @GetMapping("/exists")
    boolean existsByEventIdAndRequesterIdAndStatus(@RequestParam Long eventId,
                                                   @RequestParam Long userId,
                                                   @RequestParam RequestStatus status);

    @GetMapping("/count")
    long countByEventIdAndStatus(@RequestParam Long eventId,
                                 @RequestParam RequestStatus status);
}
