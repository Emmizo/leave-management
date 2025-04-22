package com.hr_management.hr.model;

import java.time.LocalDate;

import com.hr_management.hr.enums.LeaveDuration;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;

public class LeaveDto {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private LeaveType type;
    private EmployeeDto employee;
    private String supportingDocumentPath;
    private Double holdDays;
    private LeaveDuration leaveDuration;
    private Double numberOfDays;

    // Default constructor
    public LeaveDto() {
    }

    // All-args constructor
    public LeaveDto(Long id, LocalDate startDate, LocalDate endDate, String reason, 
                   LeaveStatus status, LeaveType type, EmployeeDto employee, 
                   String supportingDocumentPath, Double holdDays, LeaveDuration leaveDuration, 
                   Double numberOfDays) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.type = type;
        this.employee = employee;
        this.supportingDocumentPath = supportingDocumentPath;
        this.holdDays = holdDays;
        this.leaveDuration = leaveDuration;
        this.numberOfDays = numberOfDays;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private LeaveStatus status;
        private LeaveType type;
        private EmployeeDto employee;
        private String supportingDocumentPath;
        private Double holdDays;
        private LeaveDuration leaveDuration;
        private Double numberOfDays;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder status(LeaveStatus status) {
            this.status = status;
            return this;
        }

        public Builder type(LeaveType type) {
            this.type = type;
            return this;
        }

        public Builder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public Builder supportingDocumentPath(String supportingDocumentPath) {
            this.supportingDocumentPath = supportingDocumentPath;
            return this;
        }

        public Builder holdDays(Double holdDays) {
            this.holdDays = holdDays;
            return this;
        }

        public Builder leaveDuration(LeaveDuration leaveDuration) {
            this.leaveDuration = leaveDuration;
            return this;
        }

        public Builder numberOfDays(Double numberOfDays) {
            this.numberOfDays = numberOfDays;
            return this;
        }

        public LeaveDto build() {
            return new LeaveDto(id, startDate, endDate, reason, status, type, 
                               employee, supportingDocumentPath, holdDays, leaveDuration, 
                               numberOfDays);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public String getSupportingDocumentPath() {
        return supportingDocumentPath;
    }

    public void setSupportingDocumentPath(String supportingDocumentPath) {
        this.supportingDocumentPath = supportingDocumentPath;
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

    public Double getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Double numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveDto leaveDto = (LeaveDto) o;
        return (id != null ? id.equals(leaveDto.id) : leaveDto.id == null) &&
                (startDate != null ? startDate.equals(leaveDto.startDate) : leaveDto.startDate == null) &&
                (endDate != null ? endDate.equals(leaveDto.endDate) : leaveDto.endDate == null) &&
                (reason != null ? reason.equals(leaveDto.reason) : leaveDto.reason == null) &&
                status == leaveDto.status &&
                type == leaveDto.type &&
                (employee != null ? employee.equals(leaveDto.employee) : leaveDto.employee == null) &&
                (supportingDocumentPath != null ? supportingDocumentPath.equals(leaveDto.supportingDocumentPath) : leaveDto.supportingDocumentPath == null) &&
                (holdDays != null ? holdDays.equals(leaveDto.holdDays) : leaveDto.holdDays == null) &&
                leaveDuration == leaveDto.leaveDuration &&
                (numberOfDays != null ? numberOfDays.equals(leaveDto.numberOfDays) : leaveDto.numberOfDays == null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (employee != null ? employee.hashCode() : 0);
        result = 31 * result + (supportingDocumentPath != null ? supportingDocumentPath.hashCode() : 0);
        result = 31 * result + (holdDays != null ? holdDays.hashCode() : 0);
        result = 31 * result + (leaveDuration != null ? leaveDuration.hashCode() : 0);
        result = 31 * result + (numberOfDays != null ? numberOfDays.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LeaveDto{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", employee=" + employee +
                ", supportingDocumentPath='" + supportingDocumentPath + '\'' +
                ", holdDays=" + holdDays +
                ", leaveDuration=" + leaveDuration +
                ", numberOfDays=" + numberOfDays +
                '}';
    }
} 