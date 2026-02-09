package ru.yandex.practicum.stats.collector.config;

import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface CollectorClient {
    Producer<Void, UserActionAvro> getProducer();

    void stop();
}
