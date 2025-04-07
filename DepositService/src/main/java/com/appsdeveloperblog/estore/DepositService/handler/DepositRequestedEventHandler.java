package com.appsdeveloperblog.estore.DepositService.handler;

import com.appsdeveloperblog.ws.core.event.DepositRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@KafkaListener(topics = "deposit-money-topic", containerFactory = "kafkaListenerContainerFactory")
public class DepositRequestedEventHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(@Payload DepositRequestedEvent depositRequestedEvent) {
        LOGGER.info("Received a new deposit event: {} ", depositRequestedEvent.getAmount());
    }
}
