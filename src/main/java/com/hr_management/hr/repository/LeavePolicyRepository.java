package com.hr_management.hr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr_management.hr.entity.LeavePolicy;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
} 