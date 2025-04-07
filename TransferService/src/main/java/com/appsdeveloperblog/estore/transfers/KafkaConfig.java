package com.appsdeveloperblog.estore.transfers;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("withdraw-money-topic")
    private String withdrawTopicName;
    @Value("deposit-money-topic")
    private String depositTopicName;
    @Value("${spring.kafka.producer.transaction-id-prefix}")
    private String transactionIdPrefix;

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    public Map<String, Object> producerConfigs() {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix);
        return config;
    }

    @Bean
    ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    NewTopic createWithdrawTopic() {
        return TopicBuilder.name(withdrawTopicName).partitions(3).replicas(3).build();
    }

    @Bean
    NewTopic createDepositTopic() {
        return TopicBuilder.name(depositTopicName).partitions(3).replicas(3).build();
    }

}
