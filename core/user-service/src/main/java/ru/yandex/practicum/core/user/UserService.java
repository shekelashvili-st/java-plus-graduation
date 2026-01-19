package ru.yandex.practicum.core.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.user",
        "ru.yandex.practicum.stats.client",
        "ru.yandex.practicum.core.common.aspect"
})
public class UserService {
    public static void main(String[] args) {
        SpringApplication.run(UserService.class, args);
    }
}