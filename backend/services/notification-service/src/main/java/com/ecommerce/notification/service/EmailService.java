package com.ecommerce.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    /**
     * Sends an email to the user
     * 
     * @param userId  The user's ID or email
     * @param subject The email subject
     * @param content The email content
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(String userId, String subject, String content) {
        // In a real application, this would integrate with an email service like SendGrid, Mailgun, etc.
        // For demonstration purposes, we'll just log the email
        log.info("Sending email to user {}", userId);
        log.info("Subject: {}", subject);
        log.info("Content: {}", content);
        
        // Simulate successful sending
        return true;
    }
} 