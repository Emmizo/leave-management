package com.hr_management.hr.model;

import com.hr_management.hr.enums.LeaveType;

public class LeaveBalanceDto {
    private LeaveType leaveType;
    private String name;
    private int daysAvailable;
    private int daysAllowed;
    private String status;
    private String colorCode; // For UI styling (blue, green, cyan, yellow etc)

    // Default constructor
    public LeaveBalanceDto() {
    }

    // All-args constructor
    public LeaveBalanceDto(LeaveType leaveType, String name, int daysAvailable, 
                          int daysAllowed, String status, String colorCode) {
        this.leaveType = leaveType;
        this.name = name;
        this.daysAvailable = daysAvailable;
        this.daysAllowed = daysAllowed;
        this.status = status;
        this.colorCode = colorCode;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LeaveType leaveType;
        private String name;
        private int daysAvailable;
        private int daysAllowed;
        private String status;
        private String colorCode;

        public Builder leaveType(LeaveType leaveType) {
            this.leaveType = leaveType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder daysAvailable(int daysAvailable) {
            this.daysAvailable = daysAvailable;
            return this;
        }

        public Builder daysAllowed(int daysAllowed) {
            this.daysAllowed = daysAllowed;
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

        public LeaveBalanceDto build() {
            return new LeaveBalanceDto(leaveType, name, daysAvailable, daysAllowed, status, colorCode);
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

    public int getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(int daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public int getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(int daysAllowed) {
        this.daysAllowed = daysAllowed;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveBalanceDto that = (LeaveBalanceDto) o;
        return daysAvailable == that.daysAvailable &&
                daysAllowed == that.daysAllowed &&
                leaveType == that.leaveType &&
                (name != null ? name.equals(that.name) : that.name == null) &&
                (status != null ? status.equals(that.status) : that.status == null) &&
                (colorCode != null ? colorCode.equals(that.colorCode) : that.colorCode == null);
    }

    @Override
    public int hashCode() {
        int result = leaveType != null ? leaveType.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + daysAvailable;
        result = 31 * result + daysAllowed;
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
                ", status='" + status + '\'' +
                ", colorCode='" + colorCode + '\'' +
                '}';
    }
} 