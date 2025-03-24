package com.appsdeveloperblog.ws.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic createTopic() {
        Map<String, String> configs = Map.of("min.insync.replicas", "2");
        return TopicBuilder
                .name("product-crated-events-topic")
                .partitions(3)
                .replicas(3)
                .configs(configs)
                .build();
    }

}
