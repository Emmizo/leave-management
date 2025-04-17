package com.hr_management.hr.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.hr_management.hr.enums.LeaveType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeConfigDto {
    private Long id;
    private LeaveType leaveType;
    private Integer annualLimit;
    private boolean requiresDocument;
    private String description;
    private boolean active;
} 