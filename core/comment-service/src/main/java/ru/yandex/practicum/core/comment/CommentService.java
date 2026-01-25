package ru.yandex.practicum.core.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.yandex.practicum.core.comment",
        "ru.yandex.practicum.stats.client",
        "ru.yandex.practicum.core.common.aspect",
        "ru.yandex.practicum.core.common.client"
})
@EnableFeignClients(basePackages = "ru.yandex.practicum.core.common.client")
public class CommentService {
    public static void main(String[] args) {
        SpringApplication.run(CommentService.class, args);
    }
}