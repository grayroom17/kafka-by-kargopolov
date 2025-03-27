package com.appsdeveloperblog.ws.products.config;

import com.appsdeveloperblog.ws.core.event.ProductCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

import static com.appsdeveloperblog.ws.core.config.KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC;

@Configuration
public class KafkaConfig {

    private final KafkaProperties properties;

    public KafkaConfig(KafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    NewTopic createTopic() {
        Map<String, String> configs = Map.of("min.insync.replicas", "2");
        return TopicBuilder
                .name(PRODUCT_CRATED_EVENTS_TOPIC)
                .partitions(3)
                .replicas(3)
                .configs(configs)
                .build();
    }

    private Map<String, Object> producerConfigs() {
        Map<String, Object> configs = properties.buildProducerProperties();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
//        configs.put(ProducerConfig.ACKS_CONFIG, "all");
//        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
//        configs.put(ProducerConfig.RETRIES_CONFIG, 10);
        return configs;
    }

    @Bean
    public ProducerFactory<String, ProductCreatedEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
