package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.events.ProductReservedEvent;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.appsdeveloperblog.core.types.OrderStatus.CREATED;

@Component
@KafkaListener(topics = {
        "${orders.events.topic.name}",
        "${products.events.topic.name}"})
public class OrderSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;
    private final String productsCommandsTopicName;
    private final String paymentsCommandsTopicName;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     OrderHistoryService orderHistoryService,
                     @Value("${products.commands.topic.name}") String productsCommandsTopicName,
                     @Value("${payments.commands.topic.name}") String paymentsCommandsTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.productsCommandsTopicName = productsCommandsTopicName;
        this.orderHistoryService = orderHistoryService;
        this.paymentsCommandsTopicName = paymentsCommandsTopicName;
    }

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {
        ReserveProductCommand command = new ReserveProductCommand();
        BeanUtils.copyProperties(event, command);

        kafkaTemplate.send(productsCommandsTopicName, command);
        orderHistoryService.add(event.getOrderId(), CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event) {
        ProcessPaymentCommand command = new ProcessPaymentCommand();
        BeanUtils.copyProperties(event, command);
        kafkaTemplate.send(paymentsCommandsTopicName, command);
    }

}
