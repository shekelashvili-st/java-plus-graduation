package ru.yandex.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.analyzer.config.KafkaConfig;
import ru.yandex.practicum.stats.analyzer.mapper.UserActionMapper;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.storage.UserActionRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository userActionRepository;

    @Transactional
    public void handleRecord(ConsumerRecord<Void, UserActionAvro> record, KafkaConfig.ActionWeight actionWeight) {
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
