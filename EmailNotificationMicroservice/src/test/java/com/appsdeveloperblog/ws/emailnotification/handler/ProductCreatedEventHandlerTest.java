package com.appsdeveloperblog.ws.emailnotification.handler;

import com.appsdeveloperblog.ws.core.event.ProductCreatedEvent;
import com.appsdeveloperblog.ws.emailnotification.domain.repository.ProcessedEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static com.appsdeveloperblog.ws.core.config.KafkaTopics.PRODUCT_CRATED_EVENTS_TOPIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
class ProductCreatedEventHandlerTest {

    @MockitoBean
    ProcessedEventRepository processedEventRepository;
    @MockitoBean
    RestTemplate restTemplate;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    ProductCreatedEventHandler productCreatedEventHandler;

    @Test
    void handle_onProductCreated_handleEvent() throws Exception {
        //arrange
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(UUID.randomUUID().toString(), "Test title", BigDecimal.TEN, Integer.MAX_VALUE);

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreatedEvent.getId();

        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(PRODUCT_CRATED_EVENTS_TOPIC, messageKey, productCreatedEvent);
        producerRecord.headers().add("messageId", messageId.getBytes());
        producerRecord.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        doReturn(true)
                .when(processedEventRepository)
                .existsByMessageId(any());
        doReturn(null)
                .when(processedEventRepository)
                .save(any());

        String responseBody = "{\"key\":\"value\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        doReturn(responseEntity)
                .when(restTemplate)
                .exchange(anyString(), any(HttpMethod.class), isNull(), eq(String.class));

        //act
        kafkaTemplate.send(producerRecord).get();

        //assert
        ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);

        verify(productCreatedEventHandler, timeout(5000).times(1))
                .handle(eventCaptor.capture(), messageIdCaptor.capture(), messageKeyCaptor.capture());

        Assertions.assertEquals(messageId, messageIdCaptor.getValue());
        Assertions.assertEquals(messageKey, messageKeyCaptor.getValue());
        Assertions.assertEquals(productCreatedEvent.getId(), eventCaptor.getValue().getId());
    }

}