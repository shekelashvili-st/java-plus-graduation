package ru.yandex.practicum.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.aggregator.config.KafkaConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {

    private static final double PRECISION = 1e-3;
    private final KafkaConfig config;
    private Producer<Void, EventSimilarityAvro> producer;
    private Consumer<Void, UserActionAvro> consumer;
    private final KafkaConfig.ActionWeight actionWeight;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final Map<Long, Map<Long, Double>> weightMatrix = new HashMap<>();
    private final Map<Long, Double> totalSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minSums = new HashMap<>();

    @Autowired
    public AggregatorRunner(KafkaConfig config) {
        this.config = config;
        actionWeight = config.getActionWeight();
    }

    @Override
    public void run(String... args) throws Exception {
        initConsumer();
        initProducer();
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(config.getConsumer().getTopic()));
            while (true) {
                ConsumerRecords<Void, UserActionAvro> records = consumer.poll(config.getConsumer().getPollTimeout());
                for (ConsumerRecord<Void, UserActionAvro> record : records) {
                    // обрабатываем очередную запись
                    updateState(record).forEach(value ->
                            producer.send(new ProducerRecord<>(config.getProducer().getTopic(), value)));
                    // записываем оффсеты, чтобы минимизировать повторные обработки
                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }
                consumer.commitAsync((offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Во время фиксации смещений при агрегации произошла ошибка. Cмещения: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {
            // Do nothing
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                producer.flush();
                producer.close(config.getCloseTimeout());
                consumer.commitSync(currentOffsets);
                consumer.close(config.getCloseTimeout());
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void initProducer() {
        producer = new KafkaProducer<>(config.getProducer().getProperties());
    }

    private void initConsumer() {
        consumer = new KafkaConsumer<>(config.getConsumer().getProperties());
    }

    private List<EventSimilarityAvro> updateState(ConsumerRecord<Void, UserActionAvro> record) {
        List<EventSimilarityAvro> eventSimilarityList = new ArrayList<>();
        UserActionAvro userAction = record.value();
        log.info("Получено новое событие:\n{}", userAction);

        Map<Long, Double> eventWeightsForUser = weightMatrix.computeIfAbsent(userAction.getEventId(), e -> new HashMap<>());
        Double currentEventWeight = eventWeightsForUser.get(userAction.getUserId());
        Double newEventWeight = switch (userAction.getActionType()) {
            case VIEW -> actionWeight.getView();
            case REGISTER -> actionWeight.getRegistered();
            case LIKE -> actionWeight.getLike();
        };

        if (currentEventWeight == null) {
            log.trace("Пользователь взаимодействует с данным мероприятием в первый раз");
            eventWeightsForUser.put(userAction.getUserId(), newEventWeight);
            Double newTotalSum = totalSums.merge(userAction.getEventId(), newEventWeight, Double::sum);
            log.trace("Пересчитана частная сумма весов для мероприятия {}, она равна {}", userAction.getEventId(), newTotalSum);
            weightMatrix.keySet().stream()
                    .filter(eventId -> eventId != userAction.getEventId())
                    .forEach(eventId -> {
                        Double userWeight = weightMatrix.get(eventId).get(userAction.getUserId());
                        if (userWeight != null) {
                            double minNew = Double.min(userWeight, newEventWeight);
                            double newMinSum = getMinSum(eventId, userAction.getEventId()) + minNew;
                            log.trace("Пересчитана частная сумма минимумов для мероприятий {} и {}, она равна {}", userAction.getEventId(), eventId, newMinSum);
                            putMinSum(eventId, userAction.getEventId(), newMinSum);

                            double similarity = newMinSum / (Math.sqrt(newTotalSum) * Math.sqrt(totalSums.get(eventId)));
                            log.trace("Пересчитана коэффициент похожести для мероприятий {} и {}, он равен {}", userAction.getEventId(), eventId, similarity);
                            eventSimilarityList.add(EventSimilarityAvro.newBuilder()
                                    .setEventA(Long.min(eventId, userAction.getEventId()))
                                    .setEventB(Long.max(eventId, userAction.getEventId()))
                                    .setScore(similarity)
                                    .setTimestamp(Instant.now())
                                    .build());
                        }
                    });
        } else if (currentEventWeight < newEventWeight) {
            log.trace("Пользователь взаимодействует с данным мероприятием не в первый раз");
            eventWeightsForUser.put(userAction.getUserId(), newEventWeight);
            Double newTotalSum = totalSums.compute(userAction.getEventId(),
                    (key, currentSum) -> currentSum - currentEventWeight + newEventWeight);
            log.trace("Пересчитана частная сумма весов для мероприятия {}, она равна {}", userAction.getEventId(), newTotalSum);
            weightMatrix.keySet().stream()
                    .filter(eventId -> eventId != userAction.getEventId())
                    .forEach(eventId -> {
                        Double userWeight = weightMatrix.get(eventId).get(userAction.getUserId());
                        if (userWeight != null) {
                            double minOld = Double.min(userWeight, currentEventWeight);
                            double minNew = Double.min(userWeight, newEventWeight);
                            double minDelta = minNew - minOld;
                            double minSum = getMinSum(eventId, userAction.getEventId());
                            if (minDelta > PRECISION) {
                                minSum = minSum + minDelta;
                                log.trace("Пересчитана частная сумма минимумов для мероприятий {} и {}, она равна {}", userAction.getEventId(), eventId, minSum);
                                putMinSum(eventId, userAction.getEventId(), minSum);
                            }

                            double similarity = minSum / (Math.sqrt(newTotalSum) * Math.sqrt(totalSums.get(eventId)));
                            log.trace("Пересчитана коэффициент похожести для мероприятий {} и {}, он равен {}", userAction.getEventId(), eventId, similarity);
                            eventSimilarityList.add(EventSimilarityAvro.newBuilder()
                                    .setEventA(Long.min(eventId, userAction.getEventId()))
                                    .setEventB(Long.max(eventId, userAction.getEventId()))
                                    .setScore(similarity)
                                    .setTimestamp(Instant.now())
                                    .build());
                        }
                    });
        }
        log.info("К отправке подготовлено {} данных о схожести:\n{}", eventSimilarityList.size(), eventSimilarityList);
        return eventSimilarityList;
    }

    private void putMinSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double getMinSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }
}
