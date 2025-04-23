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
                        font-family: 'Segoe UI', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333333;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 650px;
                        margin: 20px auto;
                        padding: 0;
                        box-shadow: 0 3px 10px rgba(0,0,0,0.15);
                        border-radius: 8px;
                        overflow: hidden;
                    }
                    .header {
                        background-color: #184C55;
                        color: #ffffff;
                        padding: 25px;
                        text-align: center;
                    }
                    .logo {
                        margin-bottom: 15px;
                    }
                    .logo img {
                        height: 50px;
                        width: auto;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                        letter-spacing: 0.5px;
                    }
                    .content {
                        background-color: #ffffff;
                        padding: 35px;
                        border: 1px solid #e0e0e0;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px;
                        color: #666666;
                        font-size: 12px;
                        background-color: #f9f9f9;
                        border-top: 1px solid #e0e0e0;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background-color: #184C55;
                        color: #ffffff;
                        text-decoration: none;
                        border-radius: 4px;
                        margin: 25px 0;
                        font-weight: 500;
                        transition: background-color 0.3s ease;
                        text-align: center;
                    }
                    .button:hover {
                        background-color: #0d2c33;
                    }
                    .info-box {
                        background-color: #f8f9fa;
                        border-left: 4px solid #184C55;
                        padding: 25px;
                        margin: 25px 0;
                        border-radius: 4px;
                    }
                    .info-box h3 {
                        color: #184C55;
                        margin-top: 0;
                        margin-bottom: 20px;
                        font-size: 18px;
                    }
                    .status-approved {
                        color: #2E7D32;
                        font-weight: 600;
                        padding: 4px 8px;
                        background-color: #e8f5e9;
                        border-radius: 4px;
                    }
                    .status-rejected {
                        color: #C62828;
                        font-weight: 600;
                        padding: 4px 8px;
                        background-color: #ffebee;
                        border-radius: 4px;
                    }
                    .status-pending {
                        color: #F57C00;
                        font-weight: 600;
                        padding: 4px 8px;
                        background-color: #FFF3E0;
                        border-radius: 4px;
                    }
                    .details-row {
                        display: flex;
                        margin-bottom: 12px;
                    }
                    .details-label {
                        font-weight: 600;
                        color: #184C55;
                        min-width: 140px;
                    }
                    .details-value {
                        color: #333333;
                    }
                    .signature {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #eaeaea;
                    }
                    .social-icons {
                        margin-top: 15px;
                    }
                    .social-icons a {
                        display: inline-block;
                        margin: 0 5px;
                        color: #184C55;
                    }
                    @media (max-width: 600px) {
                        .container {
                            margin: 10px;
                        }
                        .content {
                            padding: 20px;
                        }
                        .details-row {
                            flex-direction: column;
                        }
                        .details-label {
                            margin-bottom: 5px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">
                            <!-- Company Logo -->
                            <img src="https://via.placeholder.com/150x50/184C55/FFFFFF?text=HR+SYSTEM" alt="Company Logo">
                        </div>
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                        <div class="signature">
                            <p>Best regards,<br><strong>HR Management System</strong></p>
                            <div class="social-icons">
                                <a href="#">LinkedIn</a> | 
                                <a href="#">Twitter</a> | 
                                <a href="#">Website</a>
                            </div>
                        </div>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Your Company. All Rights Reserved.</p>
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, title, content);
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
            
            <a href="http://localhost:5456" class="button">Log In to Your Account</a>
            
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

    public void sendLeaveRequestNotification(Leave leave, Employee employee) {
        // Email to employee
        String empTitle = "Leave Request Submitted";
        String empContent = String.format("""
            <p style="font-size: 16px;">Dear <strong>%s</strong>,</p>
            
            <p>Your leave request has been submitted and is <span class="status-pending">pending approval</span>.</p>
            
            <div class="info-box">
                <h3>Leave Request Details</h3>
                <div class="details-row">
                    <span class="details-label">Type:</span>
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
                    <span class="details-label">Duration:</span>
                    <span class="details-value">%.1f days</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Reason:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Hold Days:</span>
                    <span class="details-value">%.1f</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Leave Type:</span>
                    <span class="details-value">%s</span>
                </div>
            </div>
            
            <p>You will receive a notification once your request has been reviewed by the HR department.</p>
            
            <p>If you have any questions regarding your leave request, please contact the HR department.</p>
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
            <p style="font-size: 16px;">A new leave request requires your attention.</p>
            
            <div class="info-box">
                <h3>Employee Information</h3>
                <div class="details-row">
                    <span class="details-label">Name:</span>
                    <span class="details-value">%s %s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Employee ID:</span>
                    <span class="details-value">%d</span>
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
            
            <div class="info-box">
                <h3>Leave Request Details</h3>
                <div class="details-row">
                    <span class="details-label">Type:</span>
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
                    <span class="details-label">Duration:</span>
                    <span class="details-value">%.1f days</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Reason:</span>
                    <span class="details-value">%s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Hold Days:</span>
                    <span class="details-value">%.1f</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Leave Type:</span>
                    <span class="details-value">%s</span>
                </div>
            </div>
            
            <p>Please review this request in the HR Management System.</p>
            
            <a href="#" class="button">Review Request</a>
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
            <p style="font-size: 16px;">Dear <strong>%s</strong>,</p>
            
            <p>Your leave request has been <span class="%s">%s</span>.</p>
            
            <div class="info-box">
                <h3>Leave Request Details</h3>
                <div class="details-row">
                    <span class="details-label">Type:</span>
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
                    <span class="details-label">Duration:</span>
                    <span class="details-value">%.1f days</span>
                </div>
                %s
            </div>
            
            %s
            """,
            employee.getFirstName(),
            statusClass,
            statusText,
            leave.getLeaveType(),
            leave.getStartDate(),
            leave.getEndDate(),
            leave.getNumberOfDays(),
            newStatus == LeaveStatus.REJECTED ? 
                String.format("""
                    <div class="details-row">
                        <span class="details-label">Rejection Reason:</span>
                        <span class="details-value">%s</span>
                    </div>
                    """, rejectionReason) : "",
            newStatus == LeaveStatus.APPROVED ? 
                """
                <p>Your time off has been recorded in the HR system. If you need to cancel or modify this leave request, 
                please contact the HR department as soon as possible.</p>
                """ : 
                """
                <p>If you have questions about the rejection or would like to submit a modified request, 
                please contact the HR department.</p>
                """
        );

        if (employee.getEmail() != null) {
            emailService.sendHtmlMessage(employee.getEmail(), title, getEmailTemplate(title, content));
        }

        // Send notification to HR/Admin about the status update
        String adminTitle = "Leave Request Status Updated";
        String adminContent = String.format("""
            <p style="font-size: 16px;">A leave request status has been updated to <span class="%s">%s</span>.</p>
            
            <div class="info-box">
                <h3>Employee Information</h3>
                <div class="details-row">
                    <span class="details-label">Name:</span>
                    <span class="details-value">%s %s</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Employee ID:</span>
                    <span class="details-value">%d</span>
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
            
            <div class="info-box">
                <h3>Leave Request Details</h3>
                <div class="details-row">
                    <span class="details-label">Type:</span>
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
                    <span class="details-label">Duration:</span>
                    <span class="details-value">%.1f days</span>
                </div>
                <div class="details-row">
                    <span class="details-label">Status:</span>
                    <span class="%s">%s</span>
                </div>
                %s
            </div>
            """,
            statusClass,
            statusText,
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
                String.format("""
                    <div class="details-row">
                        <span class="details-label">Rejection Reason:</span>
                        <span class="details-value">%s</span>
                    </div>
                    """, rejectionReason) : ""
        );

        // Send admin notification
        emailService.sendHtmlMessage("hr@company.com", adminTitle, getEmailTemplate(adminTitle, adminContent));
    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        String title = "Password Reset Request";
        String resetLink = "http://localhost:5456/reset-password?token=" + resetToken;
        
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
} 