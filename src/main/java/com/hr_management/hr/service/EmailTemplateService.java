package com.hr_management.hr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.repository.UserRepository;

@Service
public class EmailTemplateService {

    private final EmailService emailService;
    private final UserRepository userRepository;

    public EmailTemplateService(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public void sendWelcomeEmail(User user, Employee employee, String plainPassword) {
        String subject = "Welcome to the HR Management System!";
        String text = String.format("""
            Hello %s,

            Welcome to the HR Management System! Your account has been successfully created.

            Account Details:
            - Username: %s
            - Password: %s
            - Email: %s
            - Department: %s
            - Position: %s

            Please log in to your account and consider changing your password for security.

            Best regards,
            HR Management Team
            """,
            employee.getFirstName(),
            user.getUsername(),
            plainPassword,
            user.getEmail(),
            employee.getDepartment(),
            employee.getPosition()
        );

        if (user.getEmail() != null) {
            emailService.sendSimpleMessage(user.getEmail(), subject, text);
        }
    }

    public void sendLeaveRequestNotification(Leave leave, Employee employee) {
        // Email to employee
        String empSubject = "Leave Request Submitted";
        String empText = String.format("""
            Dear %s,

            Your leave request has been submitted and is pending approval.

            Leave Details:
            - Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %.1f days
            - Reason: %s
            - Hold Days: %.1f
            - Leave Duration: %s

            You will be notified once your request is reviewed.

            Best regards,
            HR Department
            """,
            employee.getFirstName(),
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            leave.getReason(),
            leave.getHoldDays(),
            leave.getLeaveDuration()
        );

        if (employee.getEmail() != null) {
            emailService.sendSimpleMessage(employee.getEmail(), empSubject, empText);
        }

        // Email to HR/Admin
        String adminSubject = "New Leave Request Submitted";
        String adminText = String.format("""
            A new leave request has been submitted.

            Employee Details:
            - Name: %s %s
            - ID: %d
            - Department: %s
            - Position: %s

            Leave Details:
            - Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %.1f days
            - Reason: %s
            - Hold Days: %.1f
            - Leave Duration: %s

            Please review this request in the system.

            Best regards,
            HR Management System
            """,
            employee.getFirstName(),
            employee.getLastName(),
            employee.getId(),
            employee.getDepartment(),
            employee.getPosition(),
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            leave.getReason(),
            leave.getHoldDays(),
            leave.getLeaveDuration()
        );

        // Send to all HR/Admin users
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.HR_MANAGER)
                .toList();

        for (User admin : adminUsers) {
            if (admin.getEmail() != null) {
                emailService.sendSimpleMessage(admin.getEmail(), adminSubject, adminText);
            }
        }
    }

    public void sendLeaveStatusUpdateNotification(Leave leave, Employee employee, LeaveStatus newStatus, String rejectionReason) {
        String subject = newStatus == LeaveStatus.APPROVED ? 
            "Leave Request Approved" : "Leave Request Rejected";
        
        String text = String.format("""
            Dear %s,

            Your leave request has been %s.

            Leave Details:
            - Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %.1f days
            %s

            Best regards,
            HR Department
            """,
            employee.getFirstName(),
            newStatus == LeaveStatus.APPROVED ? "approved" : "rejected",
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            newStatus == LeaveStatus.REJECTED ? 
                String.format("- Rejection Reason: %s", rejectionReason) : ""
        );

        if (employee.getEmail() != null) {
            emailService.sendSimpleMessage(employee.getEmail(), subject, text);
        }

        // Send notification to HR/Admin about the status update
        String adminSubject = "Leave Request Status Updated";
        String adminText = String.format("""
            A leave request status has been updated.

            Employee Details:
            - Name: %s %s
            - ID: %d
            - Department: %s
            - Position: %s

            Leave Details:
            - Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %.1f days
            - Status: %s
            %s

            Best regards,
            HR Management System
            """,
            employee.getFirstName(),
            employee.getLastName(),
            employee.getId(),
            employee.getDepartment(),
            employee.getPosition(),
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            newStatus,
            newStatus == LeaveStatus.REJECTED ? 
                String.format("- Rejection Reason: %s", rejectionReason) : ""
        );

        // Send to all HR/Admin users
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.HR_MANAGER)
                .toList();

        for (User admin : adminUsers) {
            if (admin.getEmail() != null) {
                emailService.sendSimpleMessage(admin.getEmail(), adminSubject, adminText);
            }
        }
    }

    public void sendPasswordResetEmail(User user, Employee employee, String resetToken) {
        String subject = "Password Reset Request";
        String text = String.format("""
            Hello %s,

            You have requested to reset your password for the HR Management System.

            To reset your password, please use the following token:
            %s

            If you did not request a password reset, please ignore this email.

            Best regards,
            HR Management Team
            """,
            employee.getFirstName(),
            resetToken
        );

        if (user.getEmail() != null) {
            emailService.sendSimpleMessage(user.getEmail(), subject, text);
        }
    }
} 