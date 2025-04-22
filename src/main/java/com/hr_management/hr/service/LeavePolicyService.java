package com.hr_management.hr.service;

import java.util.List;

import com.hr_management.hr.entity.LeavePolicy;
import com.hr_management.hr.enums.LeaveType;

public interface LeavePolicyService {
    List<LeavePolicy> getAllLeavePolicies();
    LeavePolicy getLeavePolicyById(Long id);
    LeavePolicy createLeavePolicy(LeavePolicy leavePolicy);
    LeavePolicy updateLeavePolicy(Long id, LeavePolicy leavePolicy);
    void deleteLeavePolicy(Long id);
    int getMaxConsecutiveDays(LeaveType leaveType);
} 