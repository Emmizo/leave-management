package com.hr_management.repository;

import com.hr_management.entity.Leave;
import com.hr_management.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
} 