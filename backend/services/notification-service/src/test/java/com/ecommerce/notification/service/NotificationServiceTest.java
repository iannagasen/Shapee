package com.ecommerce.notification.service;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.model.NotificationEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private NotificationEvent emailEvent;
    private NotificationEvent smsEvent;
    private Notification emailNotification;
    private Notification smsNotification;

    @BeforeEach
    void setUp() {
        // Set up test data
        emailEvent = new NotificationEvent();
        emailEvent.setUserId("user123");
        emailEvent.setType("EMAIL");
        emailEvent.setSubject("Test Email");
        emailEvent.setContent("This is a test email");
        emailEvent.setEventType("ORDER_CREATED");

        smsEvent = new NotificationEvent();
        smsEvent.setUserId("user123");
        smsEvent.setType("SMS");
        smsEvent.setContent("This is a test SMS");
        smsEvent.setEventType("ORDER_SHIPPED");

        LocalDateTime now = LocalDateTime.now();
        
        emailNotification = Notification.builder()
                .id("1")
                .userId("user123")
                .type("EMAIL")
                .subject("Test Email")
                .content("This is a test email")
                .sent(false)
                .createdAt(now)
                .build();

        smsNotification = Notification.builder()
                .id("2")
                .userId("user123")
                .type("SMS")
                .content("This is a test SMS")
                .sent(false)
                .createdAt(now)
                .build();
    }

    @Test
    void consumeNotification_ShouldSaveAndSendEmailNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(emailNotification);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act
        notificationService.consumeNotification(emailEvent);
        
        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getAllValues().get(0);
        assertEquals(emailEvent.getUserId(), capturedNotification.getUserId());
        assertEquals(emailEvent.getType(), capturedNotification.getType());
        assertEquals(emailEvent.getSubject(), capturedNotification.getSubject());
        assertEquals(emailEvent.getContent(), capturedNotification.getContent());
        assertEquals(false, capturedNotification.isSent());
        
        verify(emailService).sendEmail(
                emailEvent.getUserId(),
                emailEvent.getSubject(),
                emailEvent.getContent()
        );
        
        Notification updatedNotification = notificationCaptor.getAllValues().get(1);
        assertTrue(updatedNotification.isSent());
    }

    @Test
    void consumeNotification_ShouldSaveAndSendSmsNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(smsNotification);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
        
        // Act
        notificationService.consumeNotification(smsEvent);
        
        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getAllValues().get(0);
        assertEquals(smsEvent.getUserId(), capturedNotification.getUserId());
        assertEquals(smsEvent.getType(), capturedNotification.getType());
        assertEquals(smsEvent.getContent(), capturedNotification.getContent());
        assertEquals(false, capturedNotification.isSent());
        
        verify(smsService).sendSms(
                smsEvent.getUserId(),
                smsEvent.getContent()
        );
        
        Notification updatedNotification = notificationCaptor.getAllValues().get(1);
        assertTrue(updatedNotification.isSent());
    }

    @Test
    void getUserNotifications_ShouldReturnUserNotifications() {
        // Arrange
        List<Notification> notifications = Arrays.asList(emailNotification, smsNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("user123")).thenReturn(notifications);
        
        // Act
        List<Notification> result = notificationService.getUserNotifications("user123");
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(notifications, result);
    }

    @Test
    void getPendingNotifications_ShouldReturnPendingNotifications() {
        // Arrange
        List<Notification> notifications = Arrays.asList(emailNotification, smsNotification);
        when(notificationRepository.findByUserIdAndSentOrderByCreatedAtDesc("user123", false)).thenReturn(notifications);
        
        // Act
        List<Notification> result = notificationService.getPendingNotifications("user123");
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(notifications, result);
    }
} 