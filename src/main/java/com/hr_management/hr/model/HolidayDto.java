package com.hr_management.hr.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDto {
    
    // ID is not needed for creation, it will be generated by the database
    private Long id;
    
    @NotBlank(message = "Holiday name is required")
    private String name;
    
    @NotNull(message = "Holiday date is required")
    private LocalDate date;
    
    private String description;
    
    private boolean recurring;
    
    // These fields are managed by the system, not needed for creation
    private String createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
} 