package com.ecommerce.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    
    private String userId;
    private String type; // EMAIL, SMS, PUSH
    private String subject;
    private String content;
    private String eventType; // ORDER_CREATED, PAYMENT_COMPLETED, SHIPPING_UPDATED, etc.
} 