package com.hr_management.hr.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatusUpdateDto {
    private Long leaveId;
    private String status;
    private String rejectionReason;
} 