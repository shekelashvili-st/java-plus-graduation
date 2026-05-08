package ru.yandex.practicum.stats.collector.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.stats.collector.config.CollectorClient;
import ru.yandex.practicum.stats.collector.mapper.UserActionMapper;

@Component
public class UserActionHandler {

    private final String topic;
    private final CollectorClient client;

    @Autowired
    public UserActionHandler(@Value("${collector.kafka.topic}") String topic,
                             CollectorClient client) {
        this.topic = topic;
        this.client = client;
    }

    public void handle(UserActionProto event) {

        UserActionAvro eventAvro = UserActionMapper.protoToAvro(event);

        ProducerRecord<Void, UserActionAvro> record = new ProducerRecord<>(topic, eventAvro);
        client.getProducer().send(record);
    }
}
