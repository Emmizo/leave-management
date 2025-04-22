package com.hr_management.hr.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendHtmlMessage(String to, String subject, String htmlContent);
    // Add more methods later if needed (e.g., for HTML emails, attachments)
} 