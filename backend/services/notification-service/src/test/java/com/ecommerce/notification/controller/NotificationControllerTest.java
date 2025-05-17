package com.ecommerce.notification.controller;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private Notification notification1;
    private Notification notification2;
    private List<Notification> notifications;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        notification1 = Notification.builder()
                .id("1")
                .userId("user123")
                .type("EMAIL")
                .subject("Test Email")
                .content("This is a test email")
                .sent(true)
                .createdAt(now.minusHours(1))
                .sentAt(now.minusHours(1).plusMinutes(5))
                .build();

        notification2 = Notification.builder()
                .id("2")
                .userId("user123")
                .type("SMS")
                .content("This is a test SMS")
                .sent(false)
                .createdAt(now)
                .build();

        notifications = Arrays.asList(notification1, notification2);
    }

    @Test
    void getUserNotifications_ShouldReturnUserNotifications() throws Exception {
        // Arrange
        when(notificationService.getUserNotifications("user123")).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/user123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].userId", is("user123")))
                .andExpect(jsonPath("$[0].type", is("EMAIL")))
                .andExpect(jsonPath("$[0].subject", is("Test Email")))
                .andExpect(jsonPath("$[0].content", is("This is a test email")))
                .andExpect(jsonPath("$[0].sent", is(true)))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].userId", is("user123")))
                .andExpect(jsonPath("$[1].type", is("SMS")))
                .andExpect(jsonPath("$[1].content", is("This is a test SMS")))
                .andExpect(jsonPath("$[1].sent", is(false)));
    }

    @Test
    void getUserNotifications_WhenNoNotifications_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(notificationService.getUserNotifications("unknown-user")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/unknown-user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPendingNotifications_ShouldReturnPendingNotifications() throws Exception {
        // Arrange
        when(notificationService.getPendingNotifications("user123")).thenReturn(Collections.singletonList(notification2));

        // Act & Assert
        mockMvc.perform(get("/api/notifications/user/user123/pending")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("2")))
                .andExpect(jsonPath("$[0].userId", is("user123")))
                .andExpect(jsonPath("$[0].type", is("SMS")))
                .andExpect(jsonPath("$[0].content", is("This is a test SMS")))
                .andExpect(jsonPath("$[0].sent", is(false)));
    }
} 