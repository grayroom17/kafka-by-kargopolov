package com.appsdeveloperblog.ws.emailnotification.handler;

import com.appsdeveloperblog.ws.core.config.KafkaTopics;
import com.appsdeveloperblog.ws.core.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC)
public class ProductCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    @KafkaHandler
    public void handle(ProductCreatedEvent productCreatedEvent) {
        LOGGER.info("***** Received product created event: {}", productCreatedEvent.getTitle());
    }

}
