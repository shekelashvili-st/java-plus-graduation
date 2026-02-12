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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.analyzer.config.KafkaConfig;
import ru.yandex.practicum.stats.analyzer.mapper.UserActionMapper;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.storage.UserActionRepository;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class UserActionProcessor implements Runnable {

    private final KafkaConfig.ConsumerConfig config;
    private final KafkaConfig.ActionWeight actionWeight;
    private final Duration closeTimeout;
    private final UserActionRepository userActionRepository;
    private Consumer<Void, UserActionAvro> consumer;

    @Autowired
    public UserActionProcessor(KafkaConfig kafkaConfig, UserActionRepository userActionRepository) {
        config = kafkaConfig.getConsumers().get(getClass().getSimpleName());
        actionWeight = kafkaConfig.getActionWeight();
        closeTimeout = kafkaConfig.getCloseTimeout();
        this.userActionRepository = userActionRepository;
    }

    @Override
    public void run() {
        initConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(config.getTopics());
            while (true) {
                ConsumerRecords<Void, UserActionAvro> records = consumer.poll(config.getPollTimeout());
                for (ConsumerRecord<Void, UserActionAvro> record : records) {
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
    private void handleRecord(ConsumerRecord<Void, UserActionAvro> record) {
        UserAction userAction = UserActionMapper.avroToModel(record.value(), actionWeight);
        boolean needUpdate = true;
        Long id = null;
        log.info("Получена информация о действии пользователя:\n{}", userAction);
        Optional<UserAction> userActionInDb = userActionRepository.findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());
        if (userActionInDb.isPresent()) {
            UserAction inDb = userActionInDb.get();
            id = inDb.getId();
            log.trace("В базе данных уже была информация о действии данного пользователя:\n{}", inDb);
            if (inDb.getRating() > userAction.getRating()) {
                needUpdate = false;
            }
        }
        if (needUpdate) {
            userAction.setId(id);
            userActionRepository.save(userAction);
            log.info("Обновлены данные в базе, так как действие новое или с большим рейтингом:\n{}", userAction);
        }
    }
}
