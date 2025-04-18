package com.appsdeveloperblog.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${payments.events.topic.name}")
    private String paymentsEventsTopicName;
    private static final Integer DEFAULT_PARTITION = 3;
    private static final Integer DEFAULT_REPLICATION_FACTOR = 3;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic paymentsEventsTopic() {
        return TopicBuilder
                .name(paymentsEventsTopicName)
                .partitions(DEFAULT_PARTITION)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

}
