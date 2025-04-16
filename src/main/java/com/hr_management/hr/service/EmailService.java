package com.hr_management.hr.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    // Add more methods later if needed (e.g., for HTML emails, attachments)
} 