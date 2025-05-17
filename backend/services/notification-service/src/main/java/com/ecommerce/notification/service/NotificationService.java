package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.model.NotificationEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    
    @KafkaListener(topics = "${notification.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotification(NotificationEvent event) {
        log.info("Received notification event: {}", event);
        
        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .type(event.getType())
                .subject(event.getSubject())
                .content(event.getContent())
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notification = notificationRepository.save(notification);
        
        sendNotification(notification);
    }
    
    public void sendNotification(Notification notification) {
        boolean sent = false;
        
        try {
            switch (notification.getType()) {
                case "EMAIL":
                    sent = emailService.sendEmail(notification.getUserId(), notification.getSubject(), notification.getContent());
                    break;
                case "SMS":
                    sent = smsService.sendSms(notification.getUserId(), notification.getContent());
                    break;
                case "PUSH":
                    // Push notification logic would go here
                    sent = true;
                    break;
                default:
                    log.warn("Unknown notification type: {}", notification.getType());
            }
            
            if (sent) {
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getPendingNotifications(String userId) {
        return notificationRepository.findByUserIdAndSentOrderByCreatedAtDesc(userId, false);
    }
} 