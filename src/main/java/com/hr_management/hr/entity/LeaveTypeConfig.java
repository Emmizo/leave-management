package com.hr_management.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import com.hr_management.hr.enums.LeaveType;

@Entity
@Data
@Table(name = "leave_type_configs")
public class LeaveTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer annualLimit;

    @Column(nullable = false)
    private Boolean requiresDocument;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;
} 