package com.hr_management.hr.service.impl;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.LeaveRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmailService;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.LeaveService;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRepository leaveRepository, EmployeeRepository employeeRepository, FileStorageService fileStorageService, EmailService emailService, UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public LeaveDto createLeaveRequest(Long employeeId, LeaveRequestDto leaveRequest, MultipartFile supportingDocument) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStartDate(leaveRequest.getStartDate());
        leave.setEndDate(leaveRequest.getEndDate());
        leave.setReason(leaveRequest.getReason());
        leave.setLeaveType(leaveRequest.getType());
        leave.setStatus(LeaveStatus.PENDING);
        // applicationDate has a default value in the entity

        // Calculate numberOfDays
        long daysBetween = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate());
        leave.setNumberOfDays((int) daysBetween + 1); // Add 1 for inclusive days

        // Store the document if provided
        if (supportingDocument != null && !supportingDocument.isEmpty()) {
            try {
                // Store file in a subdirectory named after employee ID for organization
                String filePath = fileStorageService.storeFile(supportingDocument, "employee_" + employeeId);
                leave.setSupportingDocumentPath(filePath);
            } catch (IOException e) {
                // Handle file storage exception appropriately
                throw new RuntimeException("Failed to store supporting document for leave request", e);
            }
        }

        Leave savedLeave = leaveRepository.save(leave);

        // Send email confirmation to employee
        String empSubject = "Leave Request Submitted";
        String empText = String.format(
            "Dear %s,\n\nYour leave request from %s to %s for %d day(s) has been submitted and is pending approval.\n\nReason: %s\n\nRegards,\nHR Department",
            employee.getFirstName(),
            savedLeave.getStartDate(),
            savedLeave.getEndDate(),
            savedLeave.getNumberOfDays(),
            savedLeave.getReason()
        );
        // Ensure employee email is not null before sending
        if (employee.getEmail() != null) {
            emailService.sendSimpleMessage(employee.getEmail(), empSubject, empText);
        } else {
             // Log a warning or handle cases where employee email might be missing
            System.err.println("Warning: Employee with ID " + employee.getId() + " has no email address. Cannot send leave submission notification.");
        }

        // 2. To Admins/HR Managers
        String adminSubject = "New Leave Request Submitted by " + employee.getFirstName() + " " + employee.getLastName();
        String adminText = String.format(
            "A new leave request has been submitted by %s %s (ID: %d).\n\nDates: %s to %s (%d days)\nType: %s\nReason: %s\n\nPlease review the request in the system.",
            employee.getFirstName(), employee.getLastName(), employee.getId(),
            savedLeave.getStartDate(), savedLeave.getEndDate(), savedLeave.getNumberOfDays(),
            savedLeave.getLeaveType(), savedLeave.getReason()
        );

        // TODO: Optimize this lookup later if many users exist (e.g., dedicated repository method)
        List<User> adminsAndHr = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.HR_MANAGER)
                .toList();

        for (User adminOrHr : adminsAndHr) {
            if (adminOrHr.getEmail() != null) {
                emailService.sendSimpleMessage(adminOrHr.getEmail(), adminSubject, adminText);
            } else {
                System.err.println("Warning: Admin/HR User " + adminOrHr.getUsername() + " has no email. Cannot send new leave notification.");
            }
        }

        return convertToDto(savedLeave);
    }

    @Override
    @Transactional
    public LeaveDto updateLeaveStatus(Long leaveId, String status, String rejectionReason) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        Employee employee = leave.getEmployee();
        if(employee == null) {
             throw new IllegalStateException("Leave record with ID " + leaveId + " is not associated with any employee.");
        }

        LeaveStatus oldStatus = leave.getStatus();
        LeaveStatus newStatus = LeaveStatus.valueOf(status.toUpperCase());
        leave.setStatus(newStatus);
        
        String emailSubject = "";
        String emailText = "";
        boolean sendEmail = false;

        if (newStatus == LeaveStatus.REJECTED) {
             if (rejectionReason == null || rejectionReason.isBlank()) {
                 throw new IllegalArgumentException("Rejection reason is required when rejecting a leave request.");
             }
            leave.setReason(rejectionReason);
            leave.setRejectionReason(rejectionReason);
            
            emailSubject = "Leave Request Rejected";
            emailText = String.format(
                 "Dear %s,\n\nYour leave request from %s to %s has been rejected.\n\nReason: %s\n\nRegards,\nHR Department",
                 employee.getFirstName(),
                 leave.getStartDate(),
                 leave.getEndDate(),
                 leave.getRejectionReason()
             );
             sendEmail = true;

        } else if (newStatus == LeaveStatus.APPROVED && oldStatus != LeaveStatus.APPROVED) {
             leave.setRejectionReason(null);
             
             emailSubject = "Leave Request Approved";
             emailText = String.format(
                 "Dear %s,\n\nYour leave request from %s to %s has been approved.\n\nRegards,\nHR Department",
                 employee.getFirstName(),
                 leave.getStartDate(),
                 leave.getEndDate()
             );
             sendEmail = true;
        }

        Leave updatedLeave = leaveRepository.save(leave);

        // Send email notification
        if(sendEmail && employee.getEmail() != null) {
            emailService.sendSimpleMessage(employee.getEmail(), emailSubject, emailText);
        } else if (sendEmail) {
             System.err.println("Warning: Employee with ID " + employee.getId() + " has no email address. Cannot send leave status update notification.");
        }

        return convertToDto(updatedLeave);
    }

    @Override
    public List<LeaveDto> getAllLeaveRequestsSorted() {
        List<Leave> allLeaves = leaveRepository.findAll(); // Get all leaves

        // Sort: Pending first, then by most recent application date
        allLeaves.sort(Comparator
            .<Leave, Integer>comparing(leave -> leave.getStatus() == LeaveStatus.PENDING ? 0 : 1) // Pending status gets 0, others 1
            .thenComparing(Leave::getApplicationDate, Comparator.nullsLast(Comparator.reverseOrder())) // Then by date descending
        );

        return allLeaves.stream()
                .map(this::convertToDto) // Reuse existing conversion logic
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveDto> getEmployeeLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveDto> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveDto getLeaveById(Long leaveId) {
        return leaveRepository.findById(leaveId)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
    }

    @Override
    @Transactional
    public void cancelLeaveRequest(Long leaveId, Long employeeId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Not authorized to cancel this leave request");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending leave requests");
        }
        
        // TODO: Consider deleting the supporting document when cancelling?

        leaveRepository.delete(leave);
    }

    private LeaveDto convertToDto(Leave leave) {
        EmployeeDto employeeDto = null;
        if (leave.getEmployee() != null) {
            employeeDto = convertEmployeeToDto(leave.getEmployee());
        }

        return LeaveDto.builder()
                .id(leave.getId())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .type(leave.getLeaveType())
                .employee(employeeDto)
                .supportingDocumentPath(leave.getSupportingDocumentPath())
                .build();
    }

    private EmployeeDto convertEmployeeToDto(Employee employee) {
        UserDto userDto = null;
        if (employee.getUser() != null) {
            userDto = UserDto.builder()
                    .id(employee.getUser().getId())
                    .username(employee.getUser().getUsername())
                    .email(employee.getUser().getEmail())
                    .role(employee.getUser().getRole().name())
                    .build();
        }
        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .microsoftId(employee.getMicrosoftId())
                .user(userDto)
                .build();
    }
}