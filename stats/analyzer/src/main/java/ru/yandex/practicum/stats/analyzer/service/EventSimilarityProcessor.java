package ru.yandex.practicum.stats.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.stats.analyzer.config.KafkaConfig;
import ru.yandex.practicum.stats.analyzer.storage.EventSimilarityRepository;

import java.time.Duration;

@Service
@Slf4j
public class EventSimilarityProcessor implements Runnable {

    private final KafkaConfig.ConsumerConfig config;
    private final KafkaConfig.ActionWeight actionWeight;
    private final EventSimilarityService service;
    private final Duration closeTimeout;
    private Consumer<Void, EventSimilarityAvro> consumer;

    @Autowired
    public EventSimilarityProcessor(KafkaConfig kafkaConfig, EventSimilarityRepository eventSimilarityRepository, EventSimilarityService service) {
        config = kafkaConfig.getConsumers().get(getClass().getSimpleName());
        actionWeight = kafkaConfig.getActionWeight();
        closeTimeout = kafkaConfig.getCloseTimeout();
        this.service = service;
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
                    service.handleRecord(record);
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
}
