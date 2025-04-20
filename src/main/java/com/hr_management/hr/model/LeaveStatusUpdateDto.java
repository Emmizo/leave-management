package com.hr_management.hr.model;

public class LeaveStatusUpdateDto {
    private Long leaveId;
    private String status;
    private String rejectionReason;

    // Default constructor
    public LeaveStatusUpdateDto() {
    }

    // All-args constructor
    public LeaveStatusUpdateDto(Long leaveId, String status, String rejectionReason) {
        this.leaveId = leaveId;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long leaveId;
        private String status;
        private String rejectionReason;

        public Builder leaveId(Long leaveId) {
            this.leaveId = leaveId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public LeaveStatusUpdateDto build() {
            return new LeaveStatusUpdateDto(leaveId, status, rejectionReason);
        }
    }

    // Getters and Setters
    public Long getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Long leaveId) {
        this.leaveId = leaveId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveStatusUpdateDto that = (LeaveStatusUpdateDto) o;
        return (leaveId != null ? leaveId.equals(that.leaveId) : that.leaveId == null) &&
                (status != null ? status.equals(that.status) : that.status == null) &&
                (rejectionReason != null ? rejectionReason.equals(that.rejectionReason) : that.rejectionReason == null);
    }

    @Override
    public int hashCode() {
        int result = leaveId != null ? leaveId.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (rejectionReason != null ? rejectionReason.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LeaveStatusUpdateDto{" +
                "leaveId=" + leaveId +
                ", status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
} 