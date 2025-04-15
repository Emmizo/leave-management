package com.hr_management.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequest {
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
} 