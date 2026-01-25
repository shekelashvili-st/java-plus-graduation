package ru.yandex.practicum.core.common.client.request;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import ru.yandex.practicum.core.common.dto.request.RequestStatus;

@RequiredArgsConstructor
public class RequestClientFallback implements RequestClient {
    private final Throwable cause;

    @Override
    public boolean existsByEventIdAndRequesterIdAndStatus(Long eventId, Long userId, RequestStatus status) {
        throw new NoFallbackAvailableException("Service unavailable", cause);
    }

    @Override
    public long countByEventIdAndStatus(Long eventId, RequestStatus status) {
        return 0;
    }
}
