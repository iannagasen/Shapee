package com.ecommerce.order.service;

import com.ecommerce.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${notification.kafka.topic}")
    private String notificationTopic;
    
    public void createOrder(String userId, String productId, int quantity) {
        // Logic to create an order would go here
        log.info("Creating order for user: {}, product: {}, quantity: {}", userId, productId, quantity);
        
        // Send order confirmation notification
        sendOrderConfirmation(userId, productId, quantity);
    }
    
    private void sendOrderConfirmation(String userId, String productId, int quantity) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .type("EMAIL")
                .subject("Order Confirmation")
                .content("Your order for " + quantity + " of product " + productId + " has been confirmed!")
                .eventType("ORDER_CREATED")
                .build();
        
        log.info("Sending notification event: {}", event);
        kafkaTemplate.send(notificationTopic, event);
    }
} 