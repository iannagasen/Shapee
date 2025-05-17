package com.ecommerce.notification;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.model.NotificationEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class NotificationServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("notification.kafka.topic", () -> "notification-events-test");
    }

    @TestConfiguration
    static class KafkaTestConfiguration {
        @Bean
        public ProducerFactory<String, NotificationEvent> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }
    }

    @BeforeEach
    void setup() {
        notificationRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
    }

    @Test
    void shouldConsumeEmailNotification() {
        // Given
        NotificationEvent event = new NotificationEvent();
        event.setUserId("test-user-1");
        event.setType("EMAIL");
        event.setSubject("Test Email Notification");
        event.setContent("This is a test email notification");
        event.setEventType("TEST_EVENT");

        // When
        kafkaTemplate.send("notification-events-test", event);

        // Then
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc("test-user-1");
            assertEquals(1, notifications.size());
            
            Notification notification = notifications.get(0);
            assertEquals("test-user-1", notification.getUserId());
            assertEquals("EMAIL", notification.getType());
            assertEquals("Test Email Notification", notification.getSubject());
            assertEquals("This is a test email notification", notification.getContent());
            assertTrue(notification.isSent());
        });
    }

    @Test
    void shouldConsumeSmsNotification() {
        // Given
        NotificationEvent event = new NotificationEvent();
        event.setUserId("test-user-2");
        event.setType("SMS");
        event.setContent("This is a test SMS notification");
        event.setEventType("TEST_EVENT");

        // When
        kafkaTemplate.send("notification-events-test", event);

        // Then
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc("test-user-2");
            assertEquals(1, notifications.size());
            
            Notification notification = notifications.get(0);
            assertEquals("test-user-2", notification.getUserId());
            assertEquals("SMS", notification.getType());
            assertEquals("This is a test SMS notification", notification.getContent());
            assertTrue(notification.isSent());
        });
    }
} 