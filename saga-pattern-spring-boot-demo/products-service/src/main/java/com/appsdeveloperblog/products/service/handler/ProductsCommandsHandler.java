package com.appsdeveloperblog.products.service.handler;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.core.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.events.ProductReservedEvent;
import com.appsdeveloperblog.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"${products.commands.topic.name}"})
public class ProductsCommandsHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductsCommandsHandler.class);

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productEventsTopicName;

    public ProductsCommandsHandler(ProductService productService,
                                   KafkaTemplate<String, Object> kafkaTemplate,
                                   @Value("${products.events.topic.name}") String productEventsTopicName) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productEventsTopicName = productEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            Product desiredProduct = new Product(command.getProductId(), command.getProductQuantity());
            Product reservedProduct = productService.reserve(desiredProduct, command.getOrderId());
            ProductReservedEvent event = new ProductReservedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    reservedProduct.getPrice(),
                    command.getProductQuantity()
            );
            kafkaTemplate.send(productEventsTopicName, event);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            ProductReservationFailedEvent productReservationFailedEvent = new ProductReservationFailedEvent();
            BeanUtils.copyProperties(command, productReservationFailedEvent);
            kafkaTemplate.send(productEventsTopicName, productReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelProductReservationCommand command) {
        Product productToCancel = new Product(command.getProductId(), command.getProductQuantity());
        productService.cancelReservation(productToCancel, command.getOrderId());
        ProductReservationCancelledEvent productReservationCancelledEvent = new ProductReservationCancelledEvent(command.getProductId(), command.getOrderId());
        kafkaTemplate.send(productEventsTopicName, productReservationCancelledEvent);
    }

}
