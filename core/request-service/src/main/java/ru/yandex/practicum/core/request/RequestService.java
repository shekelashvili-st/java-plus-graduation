package ru.yandex.practicum.core.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.request",
        "ru.yandex.practicum.stats.client",
        "ru.yandex.practicum.core.common.aspect"
})
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.common.client")
public class RequestService {
    public static void main(String[] args) {
        SpringApplication.run(RequestService.class, args);
    }
}