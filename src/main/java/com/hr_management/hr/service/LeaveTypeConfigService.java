package com.hr_management.hr.service;

import java.util.List;
import com.hr_management.hr.model.LeaveTypeConfigDto;
import com.hr_management.hr.enums.LeaveType;

public interface LeaveTypeConfigService {
    List<LeaveTypeConfigDto> getAllLeaveTypeConfigs();
    LeaveTypeConfigDto getLeaveTypeConfig(LeaveType leaveType);
    LeaveTypeConfigDto createLeaveTypeConfig(LeaveTypeConfigDto configDto);
    LeaveTypeConfigDto updateLeaveTypeConfig(Long id, LeaveTypeConfigDto configDto);
    void deleteLeaveTypeConfig(Long id);
    void toggleLeaveTypeStatus(LeaveType leaveType, boolean isActive);
    LeaveTypeConfigDto getLeaveTypeConfigById(Long id);
    LeaveTypeConfigDto getLeaveTypeConfigByType(LeaveType leaveType);
    boolean isLeaveTypeActive(LeaveType leaveType);
    int getAnnualLimit(LeaveType leaveType);
    boolean requiresDocument(LeaveType leaveType);
} 