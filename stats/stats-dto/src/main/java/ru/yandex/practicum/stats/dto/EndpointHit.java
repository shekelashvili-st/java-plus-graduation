package ru.yandex.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.stats.annotations.ValidIp;
import ru.yandex.practicum.stats.annotations.ValidUri;
import ru.yandex.practicum.stats.util.CustomLocalDateTimeDeserializer;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EndpointHit {
    @EqualsAndHashCode.Include
    private long id;

    @NotBlank
    private final String app;

    @ValidUri
    private final String uri;

    @ValidIp
    private final String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private final LocalDateTime timestamp;

    public EndpointHit(String app, String uri, String ip, LocalDateTime timestamp) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
        this.timestamp = timestamp;
    }
}
