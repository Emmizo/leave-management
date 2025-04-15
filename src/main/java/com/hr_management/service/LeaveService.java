package com.hr_management.service;

import com.hr_management.entity.Leave;
import com.hr_management.entity.LeaveRequest;
import java.util.List;

public interface LeaveService {
    Leave createLeaveRequest(Long employeeId, LeaveRequest leaveRequest);
    Leave updateLeaveStatus(Long leaveId, String status, String rejectionReason);
    List<Leave> getEmployeeLeaves(Long employeeId);
    List<Leave> getPendingLeaves();
    Leave getLeaveById(Long leaveId);
    void cancelLeaveRequest(Long leaveId, Long employeeId);
} 