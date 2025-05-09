package com.hr_management.hr.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of leave")
public enum LeaveType {
    @Schema(description = "Personal Time Off")
    PTO,
    
    @Schema(description = "Sick Leave")
    SICK,
    
    @Schema(description = "Compassionate Leave")
    COMPASSIONATE,
    
    @Schema(description = "Maternity Leave")
    MATERNITY,
    
    @Schema(description = "Paternity Leave")
    PATERNITY,
    
    @Schema(description = "Unpaid Leave")
    UNPAID,
    
    @Schema(description = "Other types of leave")
    OTHER
} 