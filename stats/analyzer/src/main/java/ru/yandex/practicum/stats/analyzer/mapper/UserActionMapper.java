package ru.yandex.practicum.stats.analyzer.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.analyzer.config.KafkaConfig;
import ru.yandex.practicum.stats.analyzer.model.UserAction;

@UtilityClass
public class UserActionMapper {
    public static UserAction avroToModel(UserActionAvro avro, KafkaConfig.ActionWeight actionWeight) {
        return UserAction.builder()
                .userId(avro.getUserId())
                .eventId(avro.getEventId())
                .rating(switch (avro.getActionType()) {
                    case VIEW -> actionWeight.getView();
                    case REGISTER -> actionWeight.getRegistered();
                    case LIKE -> actionWeight.getLike();
                })
                .timestamp(avro.getTimestamp())
                .build();
    }
}
