package com.hr_management.hr.service;

import java.util.List;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.LeaveStatusUpdateDto;
import org.springframework.web.multipart.MultipartFile;
import com.hr_management.hr.model.LeaveBalanceDto;

public interface LeaveService {
    LeaveDto createLeaveRequest(Long employeeId, LeaveRequestDto leaveRequest, MultipartFile supportingDocument);
    LeaveDto updateLeaveStatus(Long leaveId, LeaveStatusUpdateDto statusUpdate);
    List<LeaveDto> getEmployeeLeaves(Long employeeId);
    List<LeaveDto> getPendingLeaves();
    List<LeaveBalanceDto> getEmployeeLeaveBalances(Long employeeId);
    LeaveDto getLeaveById(Long leaveId);
    void cancelLeaveRequest(Long leaveId, Long employeeId);
    List<LeaveDto> getAllLeaveRequestsSorted();
} 