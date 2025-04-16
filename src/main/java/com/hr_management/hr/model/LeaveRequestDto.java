package com.hr_management.hr.model;

import java.time.LocalDate;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.hr_management.hr.enums.LeaveType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    // ID is removed as it's auto-generated for new requests
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveType type;
    private Long employeeId; // Keep employeeId to link the request
} 