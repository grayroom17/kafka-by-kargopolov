package com.appsdeveloperblog.ws.products.service;

import com.appsdeveloperblog.ws.core.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.products.dto.ProductCreateDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.appsdeveloperblog.ws.core.config.KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC;

@Service
public class ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProduct(ProductCreateDto dto) {
        String id = UUID.randomUUID().toString();
        //Persist Product Details into database table before publishing an Event
        ProductCreatedEvent event = new ProductCreatedEvent(id, dto.getTitle(), dto.getPrice(), dto.getQuantity());
        ProducerRecord<String, ProductCreatedEvent> producerRecord =
                new ProducerRecord<>(PRODUCT_CRATED_EVENTS_TOPIC, id, event);
        producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());
//        producerRecord.headers().add("messageId", "123".getBytes());
        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(producerRecord);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("***** Failed to send product crated event: {}", ex.getMessage());
            } else {
                LOGGER.info("***** Successfully sent product crated event: {}", result.getRecordMetadata());
            }
        });
        LOGGER.info("***** Returning product id: {}", id);
        return id;
    }

    public String createProductSync(ProductCreateDto dto) throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();
        //Persist Product Details into database table before publishing an Event
        ProductCreatedEvent event = new ProductCreatedEvent(id, dto.getTitle(), dto.getPrice(), dto.getQuantity());
        LOGGER.info("***** Before publishing a ProductCreatedEvent.");
        SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(PRODUCT_CRATED_EVENTS_TOPIC, id, event).get();
        LOGGER.info("***** Topic : {}", result.getRecordMetadata().topic());
        LOGGER.info("***** Partition : {}", result.getRecordMetadata().partition());
        LOGGER.info("***** Offset : {}", result.getRecordMetadata().offset());
        LOGGER.info("***** Returning product id: {}", id);
        return id;
    }

}