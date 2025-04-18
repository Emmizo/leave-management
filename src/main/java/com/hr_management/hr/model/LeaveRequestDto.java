package com.hr_management.hr.model;

import java.time.LocalDate;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.enums.LeaveDuration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leave request details. All fields are required except employeeId which is auto-assigned.")
public class LeaveRequestDto {
    @Schema(description = "Start date of the leave", example = "2024-03-20", required = true)
    private LocalDate startDate;

    @Schema(description = "End date of the leave", example = "2024-03-25", required = true)
    private LocalDate endDate;

    @Schema(description = "Reason for the leave request", example = "Family vacation", required = true)
    private String reason;

    @Schema(description = "Type of leave", example = "PTO", required = true, 
            allowableValues = {"PTO", "SICK", "MATERNITY", "PATERNITY", "BEREAVEMENT", "UNPAID"})
    private LeaveType type;

    @Schema(description = "Employee ID (auto-assigned, not required in request)", example = "1")
    private Long employeeId;

    @Schema(description = "Number of days to hold from annual leave balance", example = "0.0", required = true)
    private Double holdDays;

    @Schema(description = "Duration of leave for each day", example = "FULL_DAY", required = true,
            allowableValues = {"FULL_DAY", "HALF_DAY"})
    private LeaveDuration leaveDuration;
} 