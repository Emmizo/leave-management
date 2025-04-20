package com.hr_management.hr.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LeaveRequestDto {
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Leave type is required")
    private String type;

    private Long employeeId;
    private Integer holdDays;
    private String leaveDuration;
    private Integer numberOfDays;

    // Default constructor
    public LeaveRequestDto() {
    }

    // All-args constructor
    public LeaveRequestDto(LocalDate startDate, LocalDate endDate, String reason, String type, Long employeeId, Integer holdDays, String leaveDuration, Integer numberOfDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.type = type;
        this.employeeId = employeeId;
        this.holdDays = holdDays;
        this.leaveDuration = leaveDuration;
        this.numberOfDays = numberOfDays;
    }

    // Getters and Setters
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getHoldDays() {
        return holdDays;
    }

    public void setHoldDays(Integer holdDays) {
        this.holdDays = holdDays;
    }

    public String getLeaveDuration() {
        return leaveDuration;
    }

    public void setLeaveDuration(String leaveDuration) {
        this.leaveDuration = leaveDuration;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private String type;
        private Long employeeId;
        private Integer holdDays;
        private String leaveDuration;
        private Integer numberOfDays;

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

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder employeeId(Long employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder holdDays(Integer holdDays) {
            this.holdDays = holdDays;
            return this;
        }

        public Builder leaveDuration(String leaveDuration) {
            this.leaveDuration = leaveDuration;
            return this;
        }

        public Builder numberOfDays(Integer numberOfDays) {
            this.numberOfDays = numberOfDays;
            return this;
        }

        public LeaveRequestDto build() {
            return new LeaveRequestDto(startDate, endDate, reason, type, employeeId, holdDays, leaveDuration, numberOfDays);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveRequestDto that = (LeaveRequestDto) o;
        return java.util.Objects.equals(startDate, that.startDate) &&
                java.util.Objects.equals(endDate, that.endDate) &&
                java.util.Objects.equals(reason, that.reason) &&
                java.util.Objects.equals(type, that.type) &&
                java.util.Objects.equals(employeeId, that.employeeId) &&
                java.util.Objects.equals(holdDays, that.holdDays) &&
                java.util.Objects.equals(leaveDuration, that.leaveDuration) &&
                java.util.Objects.equals(numberOfDays, that.numberOfDays);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(startDate, endDate, reason, type, employeeId, holdDays, leaveDuration, numberOfDays);
    }

    @Override
    public String toString() {
        return "LeaveRequestDto{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                ", type='" + type + '\'' +
                ", employeeId=" + employeeId +
                ", holdDays=" + holdDays +
                ", leaveDuration='" + leaveDuration + '\'' +
                ", numberOfDays=" + numberOfDays +
                '}';
    }
}