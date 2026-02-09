package ru.yandex.practicum.stats.collector.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Configuration
@RequiredArgsConstructor
public class CollectorClientConfig {

    private final KafkaConfig config;

    @Bean
    public CollectorClient collectorClient() {
        return new CollectorClient() {

            private Producer<Void, UserActionAvro> producer;

            @Override
            public Producer<Void, UserActionAvro> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            @PreDestroy
            @Override
            public void stop() {
                producer.flush();
                producer.close(config.getCloseTimeout());
            }

            private void initProducer() {
                producer = new KafkaProducer<>(config.getProperties());
            }
        };
    }
}
