package com.hr_management.hr.model;

import java.time.LocalDate;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.enums.LeaveDuration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private LeaveType type;
    private EmployeeDto employee;
    private String supportingDocumentPath;
    private Double holdDays;
    private LeaveDuration leaveDuration;
} 