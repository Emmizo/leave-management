package com.hr_management.hr.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.enums.LeaveDuration;

@Entity
@Data
@Table(name = "leaves")
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer numberOfDays;

    @Column(nullable = false)
    private Double holdDays = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveDuration leaveDuration = LeaveDuration.FULL_DAY;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @Column
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDate applicationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;

    @Column(name = "supporting_document_path", nullable = true)
    private String supportingDocumentPath;
} 