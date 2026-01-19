package ru.yandex.practicum.core.main.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.main.service",
        "ru.yandex.practicum.stats.client"
})
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.common.client")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
