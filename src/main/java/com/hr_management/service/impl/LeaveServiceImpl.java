package com.hr_management.service.impl;

import com.hr_management.entity.Employee;
import com.hr_management.entity.Leave;
import com.hr_management.entity.LeaveRequest;
import com.hr_management.entity.LeaveType;
import com.hr_management.entity.LeaveStatus;
import com.hr_management.repository.EmployeeRepository;
import com.hr_management.repository.LeaveRepository;
import com.hr_management.service.LeaveService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveServiceImpl(LeaveRepository leaveRepository, EmployeeRepository employeeRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Leave createLeaveRequest(Long employeeId, LeaveRequest leaveRequest) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // Calculate number of days
        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;

        // Check if employee has enough leave balance for PTO
        if (leaveRequest.getLeaveType() == LeaveType.PTO) {
            if (employee.getAnnualLeaveBalance() < days) {
                throw new IllegalArgumentException("Insufficient leave balance");
            }
            employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() - (int) days);
        }

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveRequest.getLeaveType());
        leave.setStartDate(leaveRequest.getStartDate());
        leave.setEndDate(leaveRequest.getEndDate());
        leave.setNumberOfDays((int) days);
        leave.setReason(leaveRequest.getReason());
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRepository.save(leave);
    }

    @Override
    @Transactional
    public Leave updateLeaveStatus(Long leaveId, String status, String rejectionReason) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found"));

        LeaveStatus newStatus = LeaveStatus.valueOf(status.toUpperCase());
        leave.setStatus(newStatus);

        if (newStatus == LeaveStatus.REJECTED) {
            leave.setRejectionReason(rejectionReason);
            // If PTO was rejected, return the days to the employee's balance
            if (leave.getLeaveType() == LeaveType.PTO) {
                Employee employee = leave.getEmployee();
                employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() + leave.getNumberOfDays());
                employeeRepository.save(employee);
            }
        }

        return leaveRepository.save(leave);
    }

    @Override
    public List<Leave> getEmployeeLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<Leave> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING);
    }

    @Override
    public Leave getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found"));
    }

    @Override
    @Transactional
    public void cancelLeaveRequest(Long leaveId, Long employeeId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found"));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You can only cancel your own leave requests");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave requests can be cancelled");
        }

        // If PTO was cancelled, return the days to the employee's balance
        if (leave.getLeaveType() == LeaveType.PTO) {
            Employee employee = leave.getEmployee();
            employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() + leave.getNumberOfDays());
            employeeRepository.save(employee);
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
    }
}