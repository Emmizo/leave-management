package com.hr_management.hr.service.impl;

import com.hr_management.hr.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Set 'from' if needed/configured, otherwise it uses spring.mail.username
            // message.setFrom("noreply@example.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (MailException exception) {
            logger.error("Error sending email to {}: {}", to, exception.getMessage());
            // Depending on requirements, you might re-throw a custom exception
            // or just log the error. For now, we'll just log it.
        }
        // Consider making this @Async for better performance in a real application
    }
} 