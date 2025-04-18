package com.appsdeveloperblog.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${orders.events.topic.name}")
    private String orderEventsTopicName;
    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;
    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;
    private static final Integer DEFAULT_PARTITION = 3;
    private static final Integer DEFAULT_REPLICATION_FACTOR = 3;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic createOrderCreatedTopic() {
        return TopicBuilder
                .name(orderEventsTopicName)
                .partitions(DEFAULT_PARTITION)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createProductsCommandsTopic() {
        return TopicBuilder
                .name(productsCommandsTopicName)
                .partitions(DEFAULT_PARTITION)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createPaymentsCommandsTopic() {
        return TopicBuilder
                .name(paymentsCommandsTopicName)
                .partitions(DEFAULT_PARTITION)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

}
