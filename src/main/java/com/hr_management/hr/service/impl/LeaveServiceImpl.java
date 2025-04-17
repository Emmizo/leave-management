package com.hr_management.hr.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.model.LeaveStatusUpdateDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.LeaveRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmailService;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.LeaveService;
import com.hr_management.hr.exception.LeaveAPIException;
import com.hr_management.hr.repository.LeaveTypeConfigRepository;
import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.model.LeaveBalanceDto;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final LeaveTypeConfigRepository leaveTypeConfigRepository;

    public LeaveServiceImpl(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository,
            FileStorageService fileStorageService,
            EmailService emailService,
            UserRepository userRepository,
            LeaveTypeConfigRepository leaveTypeConfigRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.leaveTypeConfigRepository = leaveTypeConfigRepository;
    }

    @Override
    @Transactional
    public LeaveDto createLeaveRequest(Long employeeId, LeaveRequestDto leaveRequest, MultipartFile supportingDocument) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        // Calculate numberOfDays
        long daysBetween = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate());
        int numberOfDays = (int) daysBetween + 1; // Add 1 for inclusive days

        // Validate leave request based on type and balance
        validateLeaveRequest(employee, leaveRequest.getType(), numberOfDays, supportingDocument);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStartDate(leaveRequest.getStartDate());
        leave.setEndDate(leaveRequest.getEndDate());
        leave.setReason(leaveRequest.getReason());
        leave.setLeaveType(leaveRequest.getType());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setNumberOfDays(numberOfDays);

        // Store the document if provided
        if (supportingDocument != null && !supportingDocument.isEmpty()) {
            try {
                String filePath = fileStorageService.storeFile(supportingDocument, "employee_" + employeeId);
                leave.setSupportingDocumentPath(filePath);
            } catch (IOException e) {
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

        if (employee.getEmail() != null) {
            emailService.sendSimpleMessage(employee.getEmail(), empSubject, empText);
        } else {
            System.err.println("Warning: Employee with ID " + employee.getId() + " has no email address. Cannot send leave submission notification.");
        }

        // Send notification to Admins/HR Managers
        String adminSubject = "New Leave Request Submitted by " + employee.getFirstName() + " " + employee.getLastName();
        String adminText = String.format(
            "A new leave request has been submitted by %s %s (ID: %d).\n\nDates: %s to %s (%d days)\nType: %s\nReason: %s\n\nPlease review the request in the system.",
            employee.getFirstName(), employee.getLastName(), employee.getId(),
            savedLeave.getStartDate(), savedLeave.getEndDate(), savedLeave.getNumberOfDays(),
            savedLeave.getLeaveType(), savedLeave.getReason()
        );

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

    private void validateLeaveRequest(Employee employee, LeaveType leaveType, int numberOfDays, MultipartFile supportingDocument) {
        // Get leave type configuration
        LeaveTypeConfig leaveTypeConfig = leaveTypeConfigRepository.findByLeaveTypeAndIsActiveTrue(leaveType)
                .orElseThrow(() -> new LeaveAPIException(
                    HttpStatus.BAD_REQUEST,
                    "Leave type " + leaveType + " is not configured or is inactive."
                ));

        // Get the current year's leaves
        int currentYear = LocalDate.now().getYear();
        List<Leave> currentYearLeaves = leaveRepository.findByEmployeeId(employee.getId()).stream()
                .filter(leave -> leave.getStartDate().getYear() == currentYear 
                        && leave.getStatus() == LeaveStatus.APPROVED
                        && leave.getLeaveType() == leaveType)
                .toList();

        // Calculate used leave days for the current year for this specific leave type
        int usedLeaveDays = currentYearLeaves.stream()
                .mapToInt(Leave::getNumberOfDays)
                .sum();

        // Check if the request exceeds the annual limit for this leave type
        if (usedLeaveDays + numberOfDays > leaveTypeConfig.getAnnualLimit()) {
            throw new LeaveAPIException(
                HttpStatus.BAD_REQUEST,
                String.format("Insufficient leave balance for %s. You have used %d days out of %d days allowed per year. %s",
                    leaveType,
                    usedLeaveDays,
                    leaveTypeConfig.getAnnualLimit(),
                    leaveTypeConfig.getDescription())
            );
        }

        // Check if document is required
        if (leaveTypeConfig.getRequiresDocument() && (supportingDocument == null || supportingDocument.isEmpty())) {
            throw new LeaveAPIException(
                HttpStatus.BAD_REQUEST,
                String.format("Supporting document is required for %s requests. %s",
                    leaveType,
                    leaveTypeConfig.getDescription())
            );
        }
    }

    @Override
    @Transactional
    public LeaveDto updateLeaveStatus(Long leaveId, LeaveStatusUpdateDto statusUpdate) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        Employee employee = leave.getEmployee();
        if(employee == null) {
             throw new IllegalStateException("Leave record with ID " + leaveId + " is not associated with any employee.");
        }

        LeaveStatus oldStatus = leave.getStatus();
        LeaveStatus newStatus = LeaveStatus.valueOf(statusUpdate.getStatus().toUpperCase());
        leave.setStatus(newStatus);
        
        String emailSubject = "";
        String emailText = "";
        boolean sendEmail = false;

        if (newStatus == LeaveStatus.REJECTED) {
             if (statusUpdate.getRejectionReason() == null || statusUpdate.getRejectionReason().isBlank()) {
                 throw new IllegalArgumentException("Rejection reason is required when rejecting a leave request.");
             }
            leave.setReason(statusUpdate.getRejectionReason());
            leave.setRejectionReason(statusUpdate.getRejectionReason());
            
            emailSubject = "Leave Request Rejected";
            emailText = String.format(
                 "Dear %s,\n\nYour leave request from %s to %s has been rejected.\n\nReason: %s\n\nRegards,\nHR Department",
                 employee.getFirstName(),
                 leave.getStartDate(),
                 leave.getEndDate(),
                 leave.getRejectionReason()
             );
             sendEmail = true;

        } else if (newStatus == LeaveStatus.APPROVED) {
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

    @Override
    public List<LeaveBalanceDto> getEmployeeLeaveBalances(Long employeeId) {
        // Get all leave type configurations
        List<LeaveTypeConfig> allConfigs = leaveTypeConfigRepository.findAll();
        
        // Get employee's approved leaves for the current year
        int currentYear = LocalDate.now().getYear();
        List<Leave> approvedLeaves = leaveRepository.findByEmployeeId(employeeId).stream()
                .filter(leave -> leave.getStartDate().getYear() == currentYear 
                        && leave.getStatus() == LeaveStatus.APPROVED)
                .toList();

        return allConfigs.stream()
                .filter(LeaveTypeConfig::getIsActive)
                .map(config -> {
                    // Calculate used days for this leave type
                    int usedDays = approvedLeaves.stream()
                            .filter(leave -> leave.getLeaveType() == config.getLeaveType())
                            .mapToInt(Leave::getNumberOfDays)
                            .sum();
                    
                    // Calculate remaining days
                    int remainingDays = config.getAnnualLimit() - usedDays;
                    
                    // Get pending leaves for this type
                    long pendingDays = leaveRepository.findByEmployeeId(employeeId).stream()
                            .filter(leave -> 
                                leave.getStartDate().getYear() == currentYear &&
                                leave.getStatus() == LeaveStatus.PENDING &&
                                leave.getLeaveType() == config.getLeaveType())
                            .mapToInt(Leave::getNumberOfDays)
                            .sum();

                    // Determine status text
                    String status;
                    if (pendingDays > 0) {
                        status = String.format("Available (%d days pending approval)", pendingDays);
                    } else {
                        status = "Available";
                    }

                    // Determine color code based on leave type
                    String colorCode = switch (config.getLeaveType()) {
                        case PTO -> "#1976D2";  // Blue
                        case SICK -> "#2E7D32";    // Green
                        case BEREAVEMENT -> "#00BCD4"; // Cyan
                        case MATERNITY -> "#FFC107";     // Yellow
                        default -> "#757575";      // Grey
                    };

                    return LeaveBalanceDto.builder()
                            .leaveType(config.getLeaveType())
                            .name(formatLeaveName(config.getLeaveType()))
                            .daysAvailable(remainingDays)
                            .daysAllowed(config.getAnnualLimit())
                            .status(status)
                            .colorCode(colorCode)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String formatLeaveName(LeaveType leaveType) {
        return switch (leaveType) {
            case PTO -> "Annual Leave";
            case SICK -> "Sick Leave";
            case BEREAVEMENT -> "Compassionate";
            case MATERNITY -> "Maternity";
            default -> leaveType.toString();
        };
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