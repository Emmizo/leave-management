package com.hr_management.hr.model;

import java.time.LocalDate;
import java.util.List;

import com.hr_management.hr.enums.LeaveType;

public class LeaveBalanceDto {
    private LeaveType leaveType;
    private String name;
    private double daysAvailable;
    private double daysAllowed;
    private double carryForwardDays;
    private int maxCarryForwardDays;
    private String status;
    private String colorCode; // For UI styling (blue, green, cyan, yellow etc)
    private List<LeaveDateRange> leaveDates;

    // Inner class to hold leave date ranges
    public static class LeaveDateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Double numberOfDays;
        private final boolean isHalfDay;

        public LeaveDateRange(LocalDate startDate, LocalDate endDate, Double numberOfDays, boolean isHalfDay) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.numberOfDays = numberOfDays;
            this.isHalfDay = isHalfDay;
        }

        // Getters
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public Double getNumberOfDays() { return numberOfDays; }
        public boolean isHalfDay() { return isHalfDay; }
    }

    // Default constructor
    public LeaveBalanceDto() {
    }

    // All-args constructor
    public LeaveBalanceDto(LeaveType leaveType, String name, double daysAvailable, 
                          double daysAllowed, double carryForwardDays, int maxCarryForwardDays, String status, 
                          String colorCode, List<LeaveDateRange> leaveDates) {
        this.leaveType = leaveType;
        this.name = name;
        this.daysAvailable = daysAvailable;
        this.daysAllowed = daysAllowed;
        this.carryForwardDays = carryForwardDays;
        this.maxCarryForwardDays = maxCarryForwardDays;
        this.status = status;
        this.colorCode = colorCode;
        this.leaveDates = leaveDates;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LeaveType leaveType;
        private String name;
        private double daysAvailable;
        private double daysAllowed;
        private double carryForwardDays;
        private int maxCarryForwardDays;
        private String status;
        private String colorCode;
        private List<LeaveDateRange> leaveDates;

        public Builder leaveType(LeaveType leaveType) {
            this.leaveType = leaveType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder daysAvailable(double daysAvailable) {
            this.daysAvailable = daysAvailable;
            return this;
        }

        public Builder daysAllowed(double daysAllowed) {
            this.daysAllowed = daysAllowed;
            return this;
        }

        public Builder carryForwardDays(double carryForwardDays) {
            this.carryForwardDays = carryForwardDays;
            return this;
        }

        public Builder maxCarryForwardDays(int maxCarryForwardDays) {
            this.maxCarryForwardDays = maxCarryForwardDays;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder colorCode(String colorCode) {
            this.colorCode = colorCode;
            return this;
        }

        public Builder leaveDates(List<LeaveDateRange> leaveDates) {
            this.leaveDates = leaveDates;
            return this;
        }

        public LeaveBalanceDto build() {
            return new LeaveBalanceDto(leaveType, name, daysAvailable, daysAllowed, carryForwardDays, maxCarryForwardDays, status, colorCode, leaveDates);
        }
    }

    // Getters and Setters
    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(double daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public double getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(double daysAllowed) {
        this.daysAllowed = daysAllowed;
    }

    public double getCarryForwardDays() {
        return carryForwardDays;
    }

    public void setCarryForwardDays(double carryForwardDays) {
        this.carryForwardDays = carryForwardDays;
    }

    public int getMaxCarryForwardDays() {
        return maxCarryForwardDays;
    }

    public void setMaxCarryForwardDays(int maxCarryForwardDays) {
        this.maxCarryForwardDays = maxCarryForwardDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public List<LeaveDateRange> getLeaveDates() {
        return leaveDates;
    }

    public void setLeaveDates(List<LeaveDateRange> leaveDates) {
        this.leaveDates = leaveDates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveBalanceDto that = (LeaveBalanceDto) o;
        return daysAvailable == that.daysAvailable &&
                daysAllowed == that.daysAllowed &&
                carryForwardDays == that.carryForwardDays &&
                maxCarryForwardDays == that.maxCarryForwardDays &&
                leaveType == that.leaveType &&
                (name != null ? name.equals(that.name) : that.name == null) &&
                (status != null ? status.equals(that.status) : that.status == null) &&
                (colorCode != null ? colorCode.equals(that.colorCode) : that.colorCode == null);
    }

    @Override
    public int hashCode() {
        int result = leaveType != null ? leaveType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int)daysAvailable;
        result = 31 * result + (int)daysAllowed;
        result = 31 * result + (int)carryForwardDays;
        result = 31 * result + maxCarryForwardDays;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (colorCode != null ? colorCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LeaveBalanceDto{" +
                "leaveType=" + leaveType +
                ", name='" + name + '\'' +
                ", daysAvailable=" + daysAvailable +
                ", daysAllowed=" + daysAllowed +
                ", carryForwardDays=" + carryForwardDays +
                ", maxCarryForwardDays=" + maxCarryForwardDays +
                ", status='" + status + '\'' +
                ", colorCode='" + colorCode + '\'' +
                '}';
    }
} 