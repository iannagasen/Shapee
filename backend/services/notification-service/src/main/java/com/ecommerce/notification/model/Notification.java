package com.ecommerce.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    private String userId;
    private String type; // EMAIL, SMS, PUSH
    private String subject;
    private String content;
    private boolean sent;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
} 