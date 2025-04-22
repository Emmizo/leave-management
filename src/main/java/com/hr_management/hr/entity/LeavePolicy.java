package com.hr_management.hr.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "days_per_month", nullable = false)
    private Double daysPerMonth; // Auto-accrual rate (e.g., 1.66 days per month)

    @Column(name = "carry_forward_days")
    private Integer carryForwardDays;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "min_notice_days")
    private Integer minNoticeDays;

    @Column(name = "requires_approval")
    private boolean requiresApproval;

    @Column
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public LeavePolicy() {
    }

    // All-args constructor
    public LeavePolicy(Long id, String name, String description, Double daysPerMonth, 
                      Integer carryForwardDays, Integer maxConsecutiveDays, 
                      Integer minNoticeDays, boolean requiresApproval, boolean active, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.daysPerMonth = daysPerMonth;
        this.carryForwardDays = carryForwardDays;
        this.maxConsecutiveDays = maxConsecutiveDays;
        this.minNoticeDays = minNoticeDays;
        this.requiresApproval = requiresApproval;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDaysPerMonth() {
        return daysPerMonth;
    }

    public void setDaysPerMonth(Double daysPerMonth) {
        this.daysPerMonth = daysPerMonth;
    }

    public Integer getCarryForwardDays() {
        return carryForwardDays;
    }

    public void setCarryForwardDays(Integer carryForwardDays) {
        this.carryForwardDays = carryForwardDays;
    }

    public Integer getMaxConsecutiveDays() {
        return maxConsecutiveDays;
    }

    public void setMaxConsecutiveDays(Integer maxConsecutiveDays) {
        this.maxConsecutiveDays = maxConsecutiveDays;
    }

    public Integer getMinNoticeDays() {
        return minNoticeDays;
    }

    public void setMinNoticeDays(Integer minNoticeDays) {
        this.minNoticeDays = minNoticeDays;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeavePolicy that = (LeavePolicy) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "LeavePolicy{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", daysPerMonth=" + daysPerMonth +
                ", carryForwardDays=" + carryForwardDays +
                ", maxConsecutiveDays=" + maxConsecutiveDays +
                ", minNoticeDays=" + minNoticeDays +
                ", requiresApproval=" + requiresApproval +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 