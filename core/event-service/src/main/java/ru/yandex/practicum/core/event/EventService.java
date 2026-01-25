package ru.yandex.practicum.core.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.event",
        "ru.yandex.practicum.stats.client",
        "ru.yandex.practicum.core.common.aspect",
        "ru.yandex.practicum.core.common.client"
})
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.common.client")
public class EventService {
    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}
