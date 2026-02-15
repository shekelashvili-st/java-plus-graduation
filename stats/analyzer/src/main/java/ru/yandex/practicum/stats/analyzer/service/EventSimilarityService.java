package ru.yandex.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.analyzer.mapper.EventSimilarityMapper;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.storage.EventSimilarityRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void handleRecord(ConsumerRecord<Void, EventSimilarityAvro> record) {
        EventSimilarity eventSimilarity = EventSimilarityMapper.avroToModel(record.value());
        log.info("Получена информация о схожести:\n{}", eventSimilarity);
        Optional<EventSimilarity> eventSimilarityInDb = eventSimilarityRepository.findByEventAAndEventB(eventSimilarity.getEventA(), eventSimilarity.getEventB());
        eventSimilarityInDb.ifPresent(inDb -> {
            log.trace("В базе данных уже была информация о схожести, она будет заменена:\n{}", inDb);
            eventSimilarity.setId(inDb.getId());
        });
        eventSimilarityRepository.save(eventSimilarity);
        log.info("Обновлены данные в базе:\n{}", eventSimilarity);
    }
}
