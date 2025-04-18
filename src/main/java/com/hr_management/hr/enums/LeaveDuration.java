package com.hr_management.hr.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Duration of leave for each day")
public enum LeaveDuration {
    @Schema(description = "Full day leave")
    FULL_DAY,
    
    @Schema(description = "Half day leave")
    HALF_DAY
} 