package ru.yandex.practicum.stats.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.analyzer.config.KafkaConfig;
import ru.yandex.practicum.stats.analyzer.mapper.EventSimilarityMapper;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.storage.EventSimilarityRepository;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class EventSimilarityProcessor implements Runnable {

    private final KafkaConfig.ConsumerConfig config;
    private final KafkaConfig.ActionWeight actionWeight;
    private final Duration closeTimeout;
    private final EventSimilarityRepository eventSimilarityRepository;
    private Consumer<Void, EventSimilarityAvro> consumer;

    @Autowired
    public EventSimilarityProcessor(KafkaConfig kafkaConfig, EventSimilarityRepository eventSimilarityRepository) {
        config = kafkaConfig.getConsumers().get(getClass().getSimpleName());
        actionWeight = kafkaConfig.getActionWeight();
        closeTimeout = kafkaConfig.getCloseTimeout();
        this.eventSimilarityRepository = eventSimilarityRepository;
    }

    @Override
    public void run() {
        initConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(config.getTopics());
            while (true) {
                ConsumerRecords<Void, EventSimilarityAvro> records = consumer.poll(config.getPollTimeout());
                for (ConsumerRecord<Void, EventSimilarityAvro> record : records) {
                    // обрабатываем очередную запись
                    handleRecord(record);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // Do nothing
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий:", e);
        } finally {
            try {
                consumer.commitSync();
                consumer.close(closeTimeout);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void initConsumer() {
        consumer = new KafkaConsumer<>(config.getProperties());
    }

    @Transactional
    private void handleRecord(ConsumerRecord<Void, EventSimilarityAvro> record) {
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
