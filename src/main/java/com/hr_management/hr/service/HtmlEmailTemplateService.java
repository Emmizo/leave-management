package com.hr_management.hr.service;

import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.enums.LeaveStatus;

@Service
public class HtmlEmailTemplateService {

    private final EmailService emailService;

    public HtmlEmailTemplateService(EmailService emailService) {
        this.emailService = emailService;
    }

    private String getEmailTemplate(String title, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background-color: #1976D2;
                        color: white;
                        padding: 20px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        background-color: #ffffff;
                        padding: 20px;
                        border: 1px solid #dddddd;
                        border-radius: 0 0 5px 5px;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px;
                        color: #666666;
                        font-size: 12px;
                    }
                    .button {
                        display: inline-block;
                        padding: 10px 20px;
                        background-color: #1976D2;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .info-box {
                        background-color: #f5f5f5;
                        border-left: 4px solid #1976D2;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .status-approved {
                        color: #2E7D32;
                        font-weight: bold;
                    }
                    .status-rejected {
                        color: #C62828;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        <p>This is an automated message from the HR Management System.</p>
                        <p>Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, title, content);
    }

    public void sendWelcomeEmail(User user, Employee employee, String plainPassword) {
        String title = "Welcome to HR Management System";
        String content = String.format("""
            <p>Hello <strong>%s</strong>,</p>
            
            <p>Welcome to the HR Management System! Your account has been successfully created.</p>
            
            <div class="info-box">
                <h3>Account Details:</h3>
                <p><strong>Username:</strong> %s</p>
                <p><strong>Password:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Department:</strong> %s</p>
                <p><strong>Position:</strong> %s</p>
            </div>
            
            <p>Please log in to your account and consider changing your password for security.</p>
            
            <p>Best regards,<br>HR Management Team</p>
            """,
            employee.getFirstName(),
            user.getUsername(),
            plainPassword,
            user.getEmail(),
            employee.getDepartment(),
            employee.getPosition()
        );

        if (user.getEmail() != null) {
            emailService.sendHtmlMessage(user.getEmail(), title, getEmailTemplate(title, content));
        }
    }

    public void sendLeaveRequestNotification(Leave leave, Employee employee) {
        // Email to employee
        String empTitle = "Leave Request Submitted";
        String empContent = String.format("""
            <p>Dear <strong>%s</strong>,</p>
            
            <p>Your leave request has been submitted and is pending approval.</p>
            
            <div class="info-box">
                <h3>Leave Details:</h3>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Start Date:</strong> %s</p>
                <p><strong>End Date:</strong> %s</p>
                <p><strong>Duration:</strong> %.1f days</p>
                <p><strong>Reason:</strong> %s</p>
                <p><strong>Hold Days:</strong> %.1f</p>
                <p><strong>Leave Duration:</strong> %s</p>
            </div>
            
            <p>You will be notified once your request is reviewed.</p>
            
            <p>Best regards,<br>HR Department</p>
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
            emailService.sendHtmlMessage(employee.getEmail(), empTitle, getEmailTemplate(empTitle, empContent));
        }

        // Email to HR/Admin
        String adminTitle = "New Leave Request Submitted";
        String adminContent = String.format("""
            <p>A new leave request has been submitted.</p>
            
            <div class="info-box">
                <h3>Employee Details:</h3>
                <p><strong>Name:</strong> %s %s</p>
                <p><strong>ID:</strong> %d</p>
                <p><strong>Department:</strong> %s</p>
                <p><strong>Position:</strong> %s</p>
                
                <h3>Leave Details:</h3>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Start Date:</strong> %s</p>
                <p><strong>End Date:</strong> %s</p>
                <p><strong>Duration:</strong> %.1f days</p>
                <p><strong>Reason:</strong> %s</p>
                <p><strong>Hold Days:</strong> %.1f</p>
                <p><strong>Leave Duration:</strong> %s</p>
            </div>
            
            <p>Please review this request in the system.</p>
            
            <p>Best regards,<br>HR Management System</p>
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

        // Send admin notification
        emailService.sendHtmlMessage("hr@company.com", adminTitle, getEmailTemplate(adminTitle, adminContent));
    }

    public void sendLeaveStatusUpdateNotification(Leave leave, Employee employee, LeaveStatus newStatus, String rejectionReason) {
        String title = newStatus == LeaveStatus.APPROVED ? 
            "Leave Request Approved" : "Leave Request Rejected";
        
        String statusClass = newStatus == LeaveStatus.APPROVED ? "status-approved" : "status-rejected";
        String statusText = newStatus == LeaveStatus.APPROVED ? "approved" : "rejected";
        
        String content = String.format("""
            <p>Dear <strong>%s</strong>,</p>
            
            <p>Your leave request has been <span class="%s">%s</span>.</p>
            
            <div class="info-box">
                <h3>Leave Details:</h3>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Start Date:</strong> %s</p>
                <p><strong>End Date:</strong> %s</p>
                <p><strong>Duration:</strong> %.1f days</p>
                %s
            </div>
            
            <p>Best regards,<br>HR Department</p>
            """,
            employee.getFirstName(),
            statusClass,
            statusText,
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            newStatus == LeaveStatus.REJECTED ? 
                String.format("<p><strong>Rejection Reason:</strong> %s</p>", rejectionReason) : ""
        );

        if (employee.getEmail() != null) {
            emailService.sendHtmlMessage(employee.getEmail(), title, getEmailTemplate(title, content));
        }

        // Send notification to HR/Admin about the status update
        String adminTitle = "Leave Request Status Updated";
        String adminContent = String.format("""
            <p>A leave request status has been updated.</p>
            
            <div class="info-box">
                <h3>Employee Details:</h3>
                <p><strong>Name:</strong> %s %s</p>
                <p><strong>ID:</strong> %d</p>
                <p><strong>Department:</strong> %s</p>
                <p><strong>Position:</strong> %s</p>
                
                <h3>Leave Details:</h3>
                <p><strong>Type:</strong> %s</p>
                <p><strong>Start Date:</strong> %s</p>
                <p><strong>End Date:</strong> %s</p>
                <p><strong>Duration:</strong> %.1f days</p>
                <p><strong>Status:</strong> <span class="%s">%s</span></p>
                %s
            </div>
            
            <p>Best regards,<br>HR Management System</p>
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
            statusClass,
            newStatus,
            newStatus == LeaveStatus.REJECTED ? 
                String.format("<p><strong>Rejection Reason:</strong> %s</p>", rejectionReason) : ""
        );

        // Send admin notification
        emailService.sendHtmlMessage("hr@company.com", adminTitle, getEmailTemplate(adminTitle, adminContent));
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2c3e50;">Password Reset Request</h2>
                <p>Hello %s,</p>
                <p>You have requested to reset your password for the HR Management System.</p>
                <p>Please click the button below to reset your password:</p>
                <p style="text-align: center;">
                    <a href="%s" 
                       style="background-color: #3498db; 
                              color: white; 
                              padding: 12px 24px; 
                              text-decoration: none; 
                              border-radius: 4px; 
                              display: inline-block;
                              margin: 16px 0;">
                        Reset Password
                    </a>
                </p>
                <p>If the button doesn't work, you can copy and paste this link into your browser:</p>
                <p><a href="%s">%s</a></p>
                <p>This link will expire in 24 hours.</p>
                <p>If you did not request a password reset, please ignore this email.</p>
                <br>
                <p>Best regards,<br>HR Management Team</p>
            </div>
            """.formatted(
                user.getUsername(),
                resetLink,
                resetLink,
                resetLink
            );

        emailService.sendHtmlMessage(user.getEmail(), subject, htmlContent);
    }
} 