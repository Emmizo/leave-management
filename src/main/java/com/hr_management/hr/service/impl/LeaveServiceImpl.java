package com.hr_management.hr.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.enums.LeaveDuration;
import com.hr_management.hr.enums.LeaveStatus;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.exception.LeaveAPIException;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveBalanceDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.LeaveStatusUpdateDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.LeaveRepository;
import com.hr_management.hr.repository.LeaveTypeConfigRepository;
import com.hr_management.hr.service.EmailService;
import com.hr_management.hr.service.EmailTemplateService;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.LeavePolicyService;
import com.hr_management.hr.service.LeaveService;

@Service
public class LeaveServiceImpl implements LeaveService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveServiceImpl.class);
    
    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final LeaveTypeConfigRepository leaveTypeConfigRepository;
    private final EmailTemplateService emailTemplateService;
    private final LeavePolicyService leavePolicyService;

    public LeaveServiceImpl(LeaveRepository leaveRepository, 
                           EmployeeRepository employeeRepository, 
                           FileStorageService fileStorageService, 
                           EmailService emailService, 
                           LeaveTypeConfigRepository leaveTypeConfigRepository, 
                           EmailTemplateService emailTemplateService,
                           LeavePolicyService leavePolicyService) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
        this.leaveTypeConfigRepository = leaveTypeConfigRepository;
        this.emailTemplateService = emailTemplateService;
        this.leavePolicyService = leavePolicyService;
    }

    @Override
    @Transactional
    public LeaveDto createLeaveRequest(Long employeeId, LeaveRequestDto leaveRequest, MultipartFile supportingDocument) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        // Use provided numberOfDays or calculate it
        Double numberOfDays;
        if (leaveRequest.getNumberOfDays() != null) {
            numberOfDays = leaveRequest.getNumberOfDays();
        } else {
            // Calculate numberOfDays including both start and end dates
            numberOfDays = (double) ChronoUnit.DAYS.between(
                leaveRequest.getStartDate(), 
                leaveRequest.getEndDate()
            ) + 1.0; // Add 1.0 to include both start and end dates
        }

        // Handle Half-day leave duration
        if (leaveRequest.getLeaveDuration() != null) {
            try {
                LeaveDuration duration = LeaveDuration.valueOf(leaveRequest.getLeaveDuration().toUpperCase());
                if (duration == LeaveDuration.HALF_DAY) {
                    numberOfDays = 0.5;
                    // Ensure start and end dates are the same for half-day
                    if (!leaveRequest.getStartDate().equals(leaveRequest.getEndDate())) {
                        throw new LeaveAPIException(HttpStatus.BAD_REQUEST, "Start and end dates must be the same for a half-day leave.");
                    }
                }
            } catch (IllegalArgumentException e) {
                 logger.warn("Invalid leave duration value: {}. Ignoring for day calculation.", leaveRequest.getLeaveDuration());
            }
        }

        // Convert String type to LeaveType enum
        LeaveType leaveType;
        try {
            leaveType = LeaveType.valueOf(leaveRequest.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new LeaveAPIException(HttpStatus.BAD_REQUEST, 
                "Invalid leave type: " + leaveRequest.getType() + ". Valid types are: " + 
                String.join(", ", java.util.Arrays.stream(LeaveType.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toList())));
        }

        // Validate leave request based on type and balance
        validateLeaveRequest(employee, leaveType, numberOfDays, supportingDocument);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setStartDate(leaveRequest.getStartDate());
        leave.setEndDate(leaveRequest.getEndDate());
        leave.setReason(leaveRequest.getReason());
        leave.setLeaveType(leaveType);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setNumberOfDays(numberOfDays);
        
        // Convert Integer holdDays to Double
        Integer holdDaysInt = leaveRequest.getHoldDays();
        leave.setHoldDays(holdDaysInt != null ? holdDaysInt.doubleValue() : 0.0);
        
        // Convert String leaveDuration to LeaveDuration enum
        String durationStr = leaveRequest.getLeaveDuration();
        LeaveDuration duration = LeaveDuration.FULL_DAY; // Default value
        if (durationStr != null && !durationStr.isEmpty()) {
            try {
                duration = LeaveDuration.valueOf(durationStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid leave duration value: {}. Using default FULL_DAY", durationStr);
            }
        }
        leave.setLeaveDuration(duration);

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

        // Send email notifications
        emailTemplateService.sendLeaveRequestNotification(savedLeave, employee);

        return convertToDto(savedLeave);
    }

    private void validateLeaveRequest(Employee employee, LeaveType leaveType, Double numberOfDays, MultipartFile supportingDocument) {
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
        double usedLeaveDays = currentYearLeaves.stream()
                .mapToDouble(Leave::getNumberOfDays)
                .sum();

        // Calculate months worked in current year
        LocalDate now = LocalDate.now();
        int monthsWorked = now.getMonthValue(); // This gives us the current month (1-12)

        // First check against leave policy if available
        boolean hasActivePolicy = false;
        double policyDaysAllowed = 0.0;
        double policyRemainingDays = 0.0;

        var activePolicy = leavePolicyService.getAllLeavePolicies().stream()
                .filter(policy -> policy.isActive() && policy.getName().equalsIgnoreCase(leaveType.name()))
                .findFirst();

        if (activePolicy.isPresent()) {
            hasActivePolicy = true;
            policyDaysAllowed = activePolicy.get().getDaysPerMonth() * monthsWorked;
            policyRemainingDays = Math.max(0, policyDaysAllowed - usedLeaveDays);
        }

        // Calculate default annual limit
        double defaultDaysAllowed = leaveTypeConfig.getAnnualLimit() * (monthsWorked / 12.0);
        double defaultRemainingDays = Math.max(0, defaultDaysAllowed - usedLeaveDays);

        // Determine which limit to use and validate
        double remainingDays;
        double daysAllowed;
        String limitType;

        if (hasActivePolicy) {
            // Use the stricter limit between policy and default
            if (policyRemainingDays <= defaultRemainingDays) {
                remainingDays = policyRemainingDays;
                daysAllowed = policyDaysAllowed;
                limitType = "policy";
            } else {
                remainingDays = defaultRemainingDays;
                daysAllowed = defaultDaysAllowed;
                limitType = "default annual";
            }
        } else {
            remainingDays = defaultRemainingDays;
            daysAllowed = defaultDaysAllowed;
            limitType = "default annual";
        }

        // Check if the request exceeds the available balance
        if (numberOfDays > remainingDays) {
            throw new LeaveAPIException(
                HttpStatus.BAD_REQUEST,
                String.format("Insufficient leave balance for %s. You have %.1f days available out of %.1f days allowed (%s limit). %s",
                    leaveType,
                    remainingDays,
                    daysAllowed,
                    limitType,
                    leaveTypeConfig.getDescription())
            );
        }

        // Check if the request exceeds the maximum consecutive days limit from leave policy
        int maxConsecutiveDays = leavePolicyService.getMaxConsecutiveDays(leaveType);
        if (maxConsecutiveDays > 0 && numberOfDays > maxConsecutiveDays) {
            throw new LeaveAPIException(
                HttpStatus.BAD_REQUEST,
                String.format("Leave request exceeds the maximum consecutive days limit of %d days for %s.",
                    maxConsecutiveDays,
                    leaveType)
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

        LeaveStatus newStatus = LeaveStatus.valueOf(statusUpdate.getStatus().toUpperCase());
        leave.setStatus(newStatus);
        
        String emailSubject = "";
        String emailText = "";
        boolean sendEmail = false;

        if (newStatus == LeaveStatus.REJECTED) {
             if (statusUpdate.getRejectionReason() == null || statusUpdate.getRejectionReason().isBlank()) {
                 throw new IllegalArgumentException("Rejection reason is required when rejecting a leave request.");
             }
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

        // Send email notifications
        emailTemplateService.sendLeaveStatusUpdateNotification(
            updatedLeave, 
            updatedLeave.getEmployee(), 
            updatedLeave.getStatus(), 
            updatedLeave.getRejectionReason()
        );

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
        
        // Get all leaves for the employee in a single query
        int currentYear = LocalDate.now().getYear();
        List<Leave> allLeaves = leaveRepository.findByEmployeeId(employeeId);
        
        // Filter leaves for current year
        List<Leave> currentYearLeaves = allLeaves.stream()
                .filter(leave -> leave.getStartDate().getYear() == currentYear)
                .toList();

        // Calculate months worked in current year
        LocalDate now = LocalDate.now();
        int monthsWorked = now.getMonthValue(); // This gives us the current month (1-12)

        return allConfigs.stream()
                .filter(LeaveTypeConfig::getIsActive)
                .map(config -> {
                    // Get all leaves (both approved and pending) for this type
                    List<LeaveBalanceDto.LeaveDateRange> leaveDateRanges = currentYearLeaves.stream()
                            .filter(leave -> leave.getLeaveType() == config.getLeaveType())
                            .map(leave -> new LeaveBalanceDto.LeaveDateRange(
                                leave.getStartDate(),
                                leave.getEndDate(),
                                leave.getNumberOfDays(),
                                leave.getLeaveDuration() == LeaveDuration.HALF_DAY
                            ))
                            .collect(Collectors.toList());

                    // Calculate used days for this leave type (approved leaves)
                    double usedDays = currentYearLeaves.stream()
                            .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED 
                                    && leave.getLeaveType() == config.getLeaveType())
                            .mapToDouble(leave -> {
                                // If it's a half-day leave, count as 0.5 days
                                if (leave.getLeaveDuration() == LeaveDuration.HALF_DAY) {
                                    return 0.5;
                                }
                                return leave.getNumberOfDays();
                            })
                            .sum();
                    
                    // Get leave policy for this type and calculate days allowed based on months worked
                    double daysAllowed;
                    int maxCarryForwardDays = 5; // Default maximum carry-forward days
                    
                    if (config.getLeaveType() == LeaveType.MATERNITY) {
                        // For maternity leave, start with fixed 90 days per year
                        daysAllowed = 90.0;
                        
                        // Check if there's an active policy that overrides this
                        var activePolicy = leavePolicyService.getAllLeavePolicies().stream()
                                .filter(policy -> policy.isActive() && policy.getName().equalsIgnoreCase("MATERNITY"))
                                .findFirst();
                                
                        if (activePolicy.isPresent()) {
                            // If policy exists, use its daysPerMonth * 12 for annual limit
                            daysAllowed = activePolicy.get().getDaysPerMonth() * 12;
                            // Use policy's carry-forward limit if specified
                            if (activePolicy.get().getCarryForwardDays() != null) {
                                maxCarryForwardDays = activePolicy.get().getCarryForwardDays();
                            }
                        }
                    } else {
                        // For other leave types, use the normal calculation
                        var activePolicy = leavePolicyService.getAllLeavePolicies().stream()
                                .filter(policy -> policy.isActive() && policy.getName().equalsIgnoreCase(config.getLeaveType().name()))
                                .findFirst();
                                
                        if (activePolicy.isPresent()) {
                            daysAllowed = activePolicy.get().getDaysPerMonth() * monthsWorked;
                            // Use policy's carry-forward limit if specified
                            if (activePolicy.get().getCarryForwardDays() != null) {
                                maxCarryForwardDays = activePolicy.get().getCarryForwardDays();
                            }
                        } else {
                            daysAllowed = config.getAnnualLimit() * (monthsWorked / 12.0);
                        }
                    }
                    
                    // Calculate remaining days for current year
                    double remainingDays = Math.max(0, daysAllowed - usedDays);
                    
                    // Calculate carry-forward days from previous year (only for PTO)
                    double carryForwardDays = 0.0;
                    if (config.getLeaveType() == LeaveType.PTO) { // Only for PTO
                        // Get the active PTO policy to check exclusion year
                        var activePolicy = leavePolicyService.getAllLeavePolicies().stream()
                                .filter(policy -> policy.isActive() && policy.getName().equalsIgnoreCase("PTO"))
                                .findFirst();
                                
                        // Only proceed if there's an active policy and current year is after exclusion year
                        if (activePolicy.isPresent()) {
                            Integer exclusionYear = activePolicy.get().getExclusionYear();
                            if (exclusionYear != null && currentYear > exclusionYear) {
                                // Get previous year's PTO leaves for this employee
                                List<Leave> previousYearLeaves = allLeaves.stream()
                                        .filter(leave -> leave.getStartDate().getYear() == currentYear - 1 
                                                && leave.getLeaveType() == LeaveType.PTO
                                                && leave.getStatus() == LeaveStatus.APPROVED)
                                        .toList();
                                        
                                // Calculate used PTO days in previous year by checking each leave
                                double previousYearUsedDays = 0.0;
                                for (Leave leave : previousYearLeaves) {
                                    if (leave.getLeaveDuration() == LeaveDuration.HALF_DAY) {
                                        previousYearUsedDays += 0.5;
                                    } else {
                                        previousYearUsedDays += leave.getNumberOfDays();
                                    }
                                }
                                        
                                // Get previous year's allowed PTO days
                                double previousYearDaysAllowed = config.getAnnualLimit();
                                
                                // Calculate remaining PTO days from previous year
                                double previousYearRemainingDays = Math.max(0, previousYearDaysAllowed - previousYearUsedDays);
                                
                                // Only carry forward if there are remaining PTO days from previous year
                                if (previousYearRemainingDays > 0) {
                                    // Apply carry-forward limit from policy or default to 5 days
                                    Integer maxCarryForward = activePolicy.get().getCarryForwardDays();
                                    int maxCarryForwardLimit = maxCarryForward != null ? maxCarryForward : 5;
                                    carryForwardDays = Math.min(previousYearRemainingDays, maxCarryForwardLimit);
                                    
                                    // Add carry-forward days to current year's allowed PTO days
                                    daysAllowed += carryForwardDays;
                                    // Also add to remaining days since they're part of current year's PTO allowance
                                    remainingDays += carryForwardDays;
                                }
                            }
                        }
                    }
                    
                    // Round to one decimal place
                    remainingDays = Math.round(remainingDays * 10.0) / 10.0;
                    daysAllowed = Math.round(daysAllowed * 10.0) / 10.0;
                    carryForwardDays = Math.round(carryForwardDays * 10.0) / 10.0;
                    
                    // Get pending leaves for this type
                    double pendingDays = currentYearLeaves.stream()
                            .filter(leave -> leave.getStatus() == LeaveStatus.PENDING 
                                    && leave.getLeaveType() == config.getLeaveType())
                            .mapToDouble(leave -> {
                                if (leave.getLeaveDuration() == LeaveDuration.HALF_DAY) {
                                    return 0.5;
                                }
                                return leave.getNumberOfDays();
                            })
                            .sum();

                    // Determine status text
                    String status;
                    if (pendingDays > 0) {
                        status = String.format("Available (%d days pending approval)", (int) Math.ceil(pendingDays));
                    } else {
                        status = "Available";
                    }
                    
                    // Add carry-forward information to status if applicable
                    if (carryForwardDays > 0) {
                        status += String.format(" (%.1f days carried forward)", carryForwardDays);
                    }

                    // Determine color code based on leave type
                    String colorCode = switch (config.getLeaveType()) {
                        case PTO -> "#1976D2";  // Blue
                        case SICK -> "#2E7D32";    // Green
                        case COMPASSIONATE -> "#00BCD4"; // Cyan
                        case MATERNITY -> "#FFC107";     // Yellow
                        case PATERNITY -> "#9C27B0";     // Purple
                        case UNPAID -> "#757575";      // Grey
                        case OTHER -> "#FF5722";      // Orange
                        default -> "#757575";      // Grey
                    };

                    return LeaveBalanceDto.builder()
                            .leaveType(config.getLeaveType())
                            .name(formatLeaveName(config.getLeaveType()))
                            .daysAvailable(remainingDays)
                            .daysAllowed(daysAllowed)
                            .carryForwardDays(carryForwardDays)
                            .maxCarryForwardDays(maxCarryForwardDays)
                            .status(status)
                            .colorCode(colorCode)
                            .leaveDates(leaveDateRanges)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String formatLeaveName(LeaveType leaveType) {
        return switch (leaveType) {
            case PTO -> "Annual Leave";
            case SICK -> "Sick Leave";
            case COMPASSIONATE -> "Compassionate";
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
                .numberOfDays(leave.getNumberOfDays())
                .leaveDuration(leave.getLeaveDuration())
                .holdDays(leave.getHoldDays())
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
                .phone(employee.getPhone())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .microsoftId(employee.getMicrosoftId())
                .user(userDto)
                .createdAt(employee.getCreatedAt())
                .build();
    }
}