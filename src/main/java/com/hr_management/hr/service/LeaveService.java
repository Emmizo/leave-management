package com.hr_management.hr.service;

import java.util.List;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface LeaveService {
    LeaveDto createLeaveRequest(Long employeeId, LeaveRequestDto leaveRequest, MultipartFile supportingDocument);
    LeaveDto updateLeaveStatus(Long leaveId, String status, String rejectionReason);
    List<LeaveDto> getEmployeeLeaves(Long employeeId);
    List<LeaveDto> getPendingLeaves();
    LeaveDto getLeaveById(Long leaveId);
    void cancelLeaveRequest(Long leaveId, Long employeeId);
    List<LeaveDto> getAllLeaveRequestsSorted();
} 