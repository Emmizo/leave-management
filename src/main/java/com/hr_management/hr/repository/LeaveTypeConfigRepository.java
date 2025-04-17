package com.hr_management.hr.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.enums.LeaveType;

@Repository
public interface LeaveTypeConfigRepository extends JpaRepository<LeaveTypeConfig, Long> {
    Optional<LeaveTypeConfig> findByLeaveType(LeaveType leaveType);
    Optional<LeaveTypeConfig> findByLeaveTypeAndIsActiveTrue(LeaveType leaveType);
} 