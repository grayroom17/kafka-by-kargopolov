package com.appsdeveloperblog.ws.emailnotification.handler;

import com.appsdeveloperblog.ws.core.config.KafkaTopics;
import com.appsdeveloperblog.ws.core.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotification.domain.entity.ProcessedEvent;
import com.appsdeveloperblog.ws.emailnotification.domain.repository.ProcessedEventRepository;
import com.appsdeveloperblog.ws.core.error.NotRetryableException;
import com.appsdeveloperblog.ws.core.error.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;

@Component
@KafkaListener(topics = KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC/*, groupId = PRODUCT_CRATED_EVENTS_GROUP*/)
public class ProductCreatedEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    private final RestTemplate restTemplate;
    private final ProcessedEventRepository processedEventRepository;

    public ProductCreatedEventHandler(RestTemplate restTemplate,
                                      ProcessedEventRepository processedEventRepository) {
        this.restTemplate = restTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaHandler
    @Transactional
    public void handle(@Payload ProductCreatedEvent productCreatedEvent,
                       @Header("messageId") String messageId,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        LOGGER.info("***** Received product created event: {} with productId: {}", productCreatedEvent.getTitle(), productCreatedEvent.getId());
        if (processedEventRepository.existsByMessageId(messageId)) {
            LOGGER.info("Found a duplicate message id: {}", messageId);
            return;
        }
        try {
            String url = "http://localhost:8082/response/200";
            ResponseEntity<String> response = restTemplate.exchange(url, GET, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("***** Received response from a remote service: {}", response.getBody());
            }
        } catch (ResourceAccessException e) {
            LOGGER.error(e.getMessage());
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {
            LOGGER.error(e.getMessage());
            throw new NotRetryableException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new NotRetryableException(e);
        }

        try {
            processedEventRepository.save(new ProcessedEvent(null, messageId, productCreatedEvent.getId()));
        } catch (DataIntegrityViolationException e) {
            throw new NotRetryableException(e);
        }
    }

}
