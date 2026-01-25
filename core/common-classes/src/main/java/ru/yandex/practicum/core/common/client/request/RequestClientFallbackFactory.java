package ru.yandex.practicum.core.common.client.request;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClientFallback> {
    @Override
    public RequestClientFallback create(Throwable cause) {
        return new RequestClientFallback(cause);
    }
}
