package com.hr_management.hr.model;

import com.hr_management.hr.enums.LeaveType;

public class LeaveTypeConfigDto {
    private Long id;
    private LeaveType leaveType;
    private Integer annualLimit;
    private boolean requiresDocument;
    private String description;
    private boolean active;

    // Default constructor
    public LeaveTypeConfigDto() {
    }

    // All-args constructor
    public LeaveTypeConfigDto(Long id, LeaveType leaveType, Integer annualLimit, 
                             boolean requiresDocument, String description, boolean active) {
        this.id = id;
        this.leaveType = leaveType;
        this.annualLimit = annualLimit;
        this.requiresDocument = requiresDocument;
        this.description = description;
        this.active = active;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private LeaveType leaveType;
        private Integer annualLimit;
        private boolean requiresDocument;
        private String description;
        private boolean active;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder leaveType(LeaveType leaveType) {
            this.leaveType = leaveType;
            return this;
        }

        public Builder annualLimit(Integer annualLimit) {
            this.annualLimit = annualLimit;
            return this;
        }

        public Builder requiresDocument(boolean requiresDocument) {
            this.requiresDocument = requiresDocument;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public LeaveTypeConfigDto build() {
            return new LeaveTypeConfigDto(id, leaveType, annualLimit, requiresDocument, description, active);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getAnnualLimit() {
        return annualLimit;
    }

    public void setAnnualLimit(Integer annualLimit) {
        this.annualLimit = annualLimit;
    }

    public boolean isRequiresDocument() {
        return requiresDocument;
    }

    public void setRequiresDocument(boolean requiresDocument) {
        this.requiresDocument = requiresDocument;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveTypeConfigDto that = (LeaveTypeConfigDto) o;
        return requiresDocument == that.requiresDocument &&
                active == that.active &&
                (id != null ? id.equals(that.id) : that.id == null) &&
                leaveType == that.leaveType &&
                (annualLimit != null ? annualLimit.equals(that.annualLimit) : that.annualLimit == null) &&
                (description != null ? description.equals(that.description) : that.description == null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (leaveType != null ? leaveType.hashCode() : 0);
        result = 31 * result + (annualLimit != null ? annualLimit.hashCode() : 0);
        result = 31 * result + (requiresDocument ? 1 : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LeaveTypeConfigDto{" +
                "id=" + id +
                ", leaveType=" + leaveType +
                ", annualLimit=" + annualLimit +
                ", requiresDocument=" + requiresDocument +
                ", description='" + description + '\'' +
                ", active=" + active +
                '}';
    }
} 