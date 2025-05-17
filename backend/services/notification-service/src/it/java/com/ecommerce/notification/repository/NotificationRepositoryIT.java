package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Testcontainers
class NotificationRepositoryIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification sentEmailNotification;
    private Notification pendingEmailNotification;
    private Notification pendingSmsNotification;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        sentEmailNotification = Notification.builder()
                .userId("user123")
                .type("EMAIL")
                .subject("Sent Email")
                .content("This is a sent email notification")
                .sent(true)
                .createdAt(now.minusHours(2))
                .sentAt(now.minusHours(2).plusMinutes(1))
                .build();

        pendingEmailNotification = Notification.builder()
                .userId("user123")
                .type("EMAIL")
                .subject("Pending Email")
                .content("This is a pending email notification")
                .sent(false)
                .createdAt(now.minusHours(1))
                .build();

        pendingSmsNotification = Notification.builder()
                .userId("user123")
                .type("SMS")
                .content("This is a pending SMS notification")
                .sent(false)
                .createdAt(now)
                .build();

        notificationRepository.saveAll(List.of(sentEmailNotification, pendingEmailNotification, pendingSmsNotification));
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnAllUserNotificationsSortedByDate() {
        // Act
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc("user123");

        // Assert
        assertEquals(3, notifications.size());
        assertEquals(pendingSmsNotification.getContent(), notifications.get(0).getContent()); // Most recent first
        assertEquals(pendingEmailNotification.getContent(), notifications.get(1).getContent());
        assertEquals(sentEmailNotification.getContent(), notifications.get(2).getContent());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_WhenNoNotifications_ShouldReturnEmptyList() {
        // Act
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc("unknown-user");

        // Assert
        assertTrue(notifications.isEmpty());
    }

    @Test
    void findByUserIdAndSentOrderByCreatedAtDesc_ShouldReturnPendingNotifications() {
        // Act
        List<Notification> pendingNotifications = notificationRepository.findByUserIdAndSentOrderByCreatedAtDesc("user123", false);

        // Assert
        assertEquals(2, pendingNotifications.size());
        assertEquals(pendingSmsNotification.getContent(), pendingNotifications.get(0).getContent()); // Most recent first
        assertEquals(pendingEmailNotification.getContent(), pendingNotifications.get(1).getContent());
    }

    @Test
    void findByUserIdAndSentOrderByCreatedAtDesc_ShouldReturnSentNotifications() {
        // Act
        List<Notification> sentNotifications = notificationRepository.findByUserIdAndSentOrderByCreatedAtDesc("user123", true);

        // Assert
        assertEquals(1, sentNotifications.size());
        assertEquals(sentEmailNotification.getContent(), sentNotifications.get(0).getContent());
    }
} 