package com.appsdeveloperblog.ws.products.service;

import com.appsdeveloperblog.ws.products.dto.ProductCreateDto;
import com.appsdeveloperblog.ws.products.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.appsdeveloperblog.ws.products.config.KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC;

@Service
public class ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProduct(ProductCreateDto dto) {
        String id = UUID.randomUUID().toString();
        //Persist Product Details into database table before publishing an Event
        ProductCreatedEvent event = new ProductCreatedEvent(id, dto.getTitle(), dto.getPrice(), dto.getQuantity());
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(PRODUCT_CRATED_EVENTS_TOPIC, id, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("Failed to send product crated event: " + ex.getMessage());
            } else {
                LOGGER.info("Successfully sent product crated event: " + result.getRecordMetadata());
            }
        });
        future.join();
        return id;
    }

}