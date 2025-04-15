package com.hr_management.entity;

public enum LeaveStatus {
    PENDING,    // Initial status when leave is requested
    APPROVED,   // Leave request has been approved
    REJECTED,   // Leave request has been rejected
    CANCELLED   // Leave request has been cancelled by the employee
} 