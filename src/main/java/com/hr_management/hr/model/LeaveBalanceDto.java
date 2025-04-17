package com.hr_management.hr.model;

import com.hr_management.hr.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveBalanceDto {
    private LeaveType leaveType;
    private String name;
    private int daysAvailable;
    private int daysAllowed;
    private String status;
    private String colorCode; // For UI styling (blue, green, cyan, yellow etc)
} 