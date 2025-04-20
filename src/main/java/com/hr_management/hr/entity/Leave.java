package com.hr_management.hr.entity;

import java.time.LocalDate;

import com.hr_management.hr.enums.LeaveDuration;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;

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

@Entity
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

    // Default constructor
    public Leave() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public Double getHoldDays() {
        return holdDays;
    }

    public void setHoldDays(Double holdDays) {
        this.holdDays = holdDays;
    }

    public LeaveDuration getLeaveDuration() {
        return leaveDuration;
    }

    public void setLeaveDuration(LeaveDuration leaveDuration) {
        this.leaveDuration = leaveDuration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public String getSupportingDocumentPath() {
        return supportingDocumentPath;
    }

    public void setSupportingDocumentPath(String supportingDocumentPath) {
        this.supportingDocumentPath = supportingDocumentPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Leave leave = (Leave) o;
        return id != null && id.equals(leave.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Leave{" +
                "id=" + id +
                ", employee=" + (employee != null ? employee.getId() : null) +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", numberOfDays=" + numberOfDays +
                ", holdDays=" + holdDays +
                ", leaveDuration=" + leaveDuration +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", applicationDate=" + applicationDate +
                ", leaveType=" + leaveType +
                ", supportingDocumentPath='" + supportingDocumentPath + '\'' +
                '}';
    }
} 