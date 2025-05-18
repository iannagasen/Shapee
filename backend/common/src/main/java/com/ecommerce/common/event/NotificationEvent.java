package com.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common event used to trigger notifications across the system.
 * This event can be published by any service and consumed by the notification service.
 */
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