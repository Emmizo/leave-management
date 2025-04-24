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
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 0 10px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #184C55;
                        color: #ffffff;
                        padding: 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        padding: 20px;
                        color: #333333;
                    }
                    .footer {
                        background-color: #184C55;
                        color: #ffffff;
                        text-align: center;
                        padding: 15px;
                        font-size: 14px;
                    }
                    .button {
                        display: inline-block;
                        background-color: #184C55;
                        color: #ffffff;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 4px;
                        margin: 20px 0;
                    }
                    .info-box {
                        background-color: #f8f9fa;
                        border: 1px solid #e9ecef;
                        border-radius: 4px;
                        padding: 15px;
                        margin: 15px 0;
                    }
                    .info-box h3 {
                        color: #184C55;
                        margin-top: 0;
                        margin-bottom: 15px;
                        font-size: 18px;
                    }
                    .details-row {
                        display: flex;
                        margin-bottom: 8px;
                        font-size: 14px;
                    }
                    .details-label {
                        font-weight: bold;
                        width: 120px;
                        color: #184C55;
                    }
                    .details-value {
                        flex: 1;
                    }
                    .status-pending {
                        color: #ffa500;
                        font-weight: bold;
                    }
                    .status-approved {
                        color: #28a745;
                        font-weight: bold;
                    }
                    .status-rejected {
                        color: #dc3545;
                        font-weight: bold;
                    }
                    ol, ul {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    li {
                        margin-bottom: 8px;
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
                        <p>© 2024 HR Management System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            title,
            content
        );
    }

    public void sendWelcomeEmail(User user, Employee employee, String plainPassword) {
        String title = "Welcome to HR Management System";
        String content = String.format("""
            <p style="font-size: 16px;">Hello <strong>%s</strong>,</p>
            
            <p>Welcome to the HR Management System! Your employee account has been successfully created and is ready to use.</p>
            
            <div class="info-box">
                <h3>Your Account Details</h3>
                <div class="details-row">
                    <span class="details-label">Username:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Temporary Password:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Email:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Department:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Position:</span>
                    <span class="details-value">%s</span>
                </div>
            </div>
            
            <p><strong>Next Steps:</strong></p>
            <ol>
                <li>Log in using the credentials above</li>
                <li>Change your temporary password immediately</li>
                <li>Complete your employee profile</li>
                <li>Explore the HR Management System features</li>
            </ol>
            
            <p>For security reasons, please change your password upon first login.</p>
            
            
            
            <p>If you have any questions or need assistance, please contact the HR department.</p>
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

    public void sendLeaveRequestNotification(Leave leave, String recipientEmail, boolean isHrAdmin) {
        String title = "Leave Request Notification";
        String content;
        
        if (isHrAdmin) {
            content = String.format("""
                <div class="info-box">
                    <h3>New Leave Request Details</h3>
                    <div class="details-row">
                        <span class="details-label">Employee:</span>
                        <span class="details-value">%s %s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Leave Type:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Start Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">End Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Status:</span>
                        <span class="details-value status-pending">Pending</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Reason:</span>
                        <span class="details-value">%s</span>
                    </div>
                </div>
                <p>Please review this leave request and take appropriate action:</p>
                <ol>
                    <li>Log in to the HR Management System</li>
                    <li>Navigate to the Leave Requests section</li>
                    <li>Review the request details and supporting documents (if any)</li>
                    <li>Approve or reject the request based on company policy</li>
                </ol>
                <a href="%s" class="button">View Leave Request</a>
                """,
                leave.getEmployee().getFirstName(),
                leave.getEmployee().getLastName(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                "http://localhost:3000/leave-history" // Replace with actual URL
            );
        } else {
            content = String.format("""
                <div class="info-box">
                    <h3>Your Leave Request Has Been Submitted</h3>
                    <div class="details-row">
                        <span class="details-label">Leave Type:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Start Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">End Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Status:</span>
                        <span class="details-value status-pending">Pending</span>
                    </div>
                </div>
                <p>Your leave request has been successfully submitted and is pending approval. Here's what happens next:</p>
                <ul>
                    <li>HR will review your request within 1-2 business days</li>
                    <li>You will receive an email notification once your request is approved or rejected</li>
                    <li>You can track the status of your request in the HR Management System</li>
                </ul>
                <a href="%s" class="button">View Request Status</a>
                """,
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                "http://localhost:3000/leave-history" // Replace with actual URL
            );
        }
        
        String emailContent = getEmailTemplate(title, content);
        emailService.sendHtmlMessage(recipientEmail, title, emailContent);
    }

    public void sendLeaveStatusUpdateNotification(Leave leave, String recipientEmail, boolean isHrAdmin) {
        String title = "Leave Request Status Update";
        String statusClass = leave.getStatus() == LeaveStatus.APPROVED ? "status-approved" : "status-rejected";
        String content;
        
        if (isHrAdmin) {
            content = String.format("""
                <div class="info-box">
                    <h3>Leave Request Status Updated</h3>
                    <div class="details-row">
                        <span class="details-label">Employee:</span>
                        <span class="details-value">%s %s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Leave Type:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Status:</span>
                        <span class="details-value %s">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Comments:</span>
                        <span class="details-value">%s</span>
                    </div>
                </div>
                <p>The leave request has been %s. The employee has been notified of this update.</p>
                <a href="%s" class="button">View Leave Request</a>
                """,
                leave.getEmployee().getFirstName(),
                leave.getEmployee().getLastName(),
                leave.getLeaveType(),
                statusClass,
                leave.getStatus(),
                leave.getRejectionReason(),
                leave.getStatus().toString().toLowerCase(),
                "http://localhost:3000/leave-history" // Replace with actual URL
            );
        } else {
            content = String.format("""
                <div class="info-box">
                    <h3>Your Leave Request Has Been %s</h3>
                    <div class="details-row">
                        <span class="details-label">Leave Type:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Start Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">End Date:</span>
                        <span class="details-value">%s</span>
                    </div>
                    <div class="details-row">
                        <span class="details-label">Status:</span>
                        <span class="details-value %s">%s</span>
                    </div>
                    %s
                </div>
                %s
                <a href="%s" class="button">View Leave Details</a>
                """,
                leave.getStatus(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                statusClass,
                leave.getStatus(),
                leave.getRejectionReason() != null && !leave.getRejectionReason().isEmpty() 
                    ? String.format("""
                        <div class="details-row">
                            <span class="details-label">Comments:</span>
                            <span class="details-value">%s</span>
                        </div>
                        """, leave.getRejectionReason()) 
                    : "",
                leave.getStatus() == LeaveStatus.APPROVED 
                    ? "<p>Your leave request has been approved. Please ensure you:</p><ul><li>Update your team about your absence</li><li>Set up an out-of-office message</li><li>Complete any necessary handover documentation</li></ul>"
                    : "<p>Your leave request has been rejected. If you have any questions about this decision, please contact your supervisor or HR department.</p>",
                "http://localhost:8080/my-leaves" // Replace with actual URL
            );
        }
        
        String emailContent = getEmailTemplate(title, content);
        emailService.sendHtmlMessage(recipientEmail, title, emailContent);
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        String title = "Password Reset Request";
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        
        String content = String.format("""
            <p style="font-size: 16px;">Hello <strong>%s</strong>,</p>
            
            <p>We received a request to reset your password for the HR Management System.</p>
            
            <div class="info-box">
                <h3>Password Reset Information</h3>
                <div class="details-row">
                    <span class="details-label">Username:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Email:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Request Time:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Expires:</span>
                    <span class="details-value">24 hours from request</span>
                </div>
            </div>
            
            <p>To reset your password, please click the button below:</p>
            
            <div style="text-align: center; margin: 30px 0;">
                <a href="%s" class="button">Reset Your Password</a>
            </div>
            
            <p>If the button doesn't work, you can copy and paste this link into your browser:</p>
            <p style="background-color: #f5f5f5; padding: 10px; border-radius: 4px; word-break: break-all;">
                <a href="%s">%s</a>
            </p>
            
            <p><strong>Important:</strong> This link will expire in 24 hours. If you did not request a password reset, please ignore this email or contact the HR department immediately if you have concerns.</p>
            """,
            user.getUsername(),
            user.getUsername(),
            user.getEmail(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            resetLink,
            resetLink,
            resetLink
        );

        if (user.getEmail() != null) {
            emailService.sendHtmlMessage(user.getEmail(), title, getEmailTemplate(title, content));
        }
    }

    public void sendPasswordResetConfirmationEmail(User user) {
        String title = "Password Reset Confirmation";
        
        String content = String.format("""
            <p style="font-size: 16px;">Hello <strong>%s</strong>,</p>
            
            <p>Your password has been successfully reset for the HR Management System.</p>
            
            <div class="info-box">
                <h3>Account Information</h3>
                <div class="details-row">
                    <span class="details-label">Username:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Email:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Reset Time:</span>
                    <span class="details-value">%s</span>
                </div>
            </div>
            
            <p><strong>Important:</strong> If you did not request this password reset, please contact the HR department immediately.</p>
            
            <p>You can now log in to your account using your new password.</p>
            """,
            user.getUsername(),
            user.getUsername(),
            user.getEmail(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        if (user.getEmail() != null) {
            emailService.sendHtmlMessage(user.getEmail(), title, getEmailTemplate(title, content));
        }
    }
} 