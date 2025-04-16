package com.hr_management.hr.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String position;
    private Integer annualLeaveBalance;
    private String microsoftId;
    private UserDto user;
    private List<LeaveDto> leaves;
} 