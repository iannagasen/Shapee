package com.ecommerce.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    
    /**
     * Sends an SMS to the user
     * 
     * @param userId  The user's ID or phone number
     * @param content The SMS content
     * @return true if the SMS was sent successfully, false otherwise
     */
    public boolean sendSms(String userId, String content) {
        // In a real application, this would integrate with an SMS service like Twilio, Nexmo, etc.
        // For demonstration purposes, we'll just log the SMS
        log.info("Sending SMS to user {}", userId);
        log.info("Content: {}", content);
        
        // Simulate successful sending
        return true;
    }
} 