package ru.yandex.practicum.stats.analyzer.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;

import java.util.Objects;

@UtilityClass
public class EventSimilarityMapper {
    public static EventSimilarity avroToModel(EventSimilarityAvro avro) {
        return EventSimilarity.builder()
                .eventA(avro.getEventA())
                .eventB(avro.getEventB())
                .score(avro.getScore())
                .timestamp(avro.getTimestamp())
                .build();
    }

    public static RecommendedEventProto scoreToEvent(EventSimilarity model, Long eventId) {
        Long eventA = model.getEventA();
        Long eventB = model.getEventB();
        return RecommendedEventProto.newBuilder()
                .setEventId(!Objects.equals(eventA, eventId) ? eventA : eventB)
                .setScore(model.getScore())
                .build();
    }
}
