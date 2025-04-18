package com.hr_management.hr.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveBalanceDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.LeaveStatusUpdateDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.LeaveService;
import com.hr_management.hr.exception.LeaveAPIException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "Leave request management APIs")
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveController(LeaveService leaveService, EmployeeService employeeService, UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.leaveService = leaveService;
        this.employeeService = employeeService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/history")
    @Operation(summary = "Get Leave History (Role-Based)", 
               description = "Retrieves leave history. Admins/HR Managers see all sorted history. Regular users see their own history.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee record not found for the user (if fetching own history)")
    })
    public ResponseEntity<List<LeaveDto>> getLeaveHistory(Authentication authentication) { 
        if (authentication == null) {
             return ResponseEntity.status(401).build(); 
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdminOrHr = authorities.stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN") || 
                                         grantedAuthority.getAuthority().equals("ROLE_HR_MANAGER"));

        if (isAdminOrHr) {
            return ResponseEntity.ok(leaveService.getAllLeaveRequestsSorted());
        } else {
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                System.err.println("Principal is null after authentication");
                return ResponseEntity.status(403).build();
            }
            
            if (principal instanceof User currentUser) {
                Long employeeId = getEmployeeIdFromUser(currentUser);
                return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId));
            } else {
                 System.err.println("Unexpected principal type: " + principal.getClass().getName());
                 return ResponseEntity.status(403).build();
            }
        }
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Create a new leave request", 
               description = """
               Creates a new leave request. Supports two formats:
               
               1. JSON Request (Content-Type: application/json):
                  Required fields: startDate, endDate, reason, type
                  Optional fields: holdDays (default: 0.0), leaveDuration (FULL_DAY/HALF_DAY)
                  Note: Use this format for leave types that don't require documentation
               
               2. Multipart Request (Content-Type: multipart/form-data):
                  - leaveRequest: JSON object with the above fields
                  - document: Optional file upload (required for certain leave types like SICK or MATERNITY)
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee record not found"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    public ResponseEntity<?> createLeaveRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @RequestPart(value = "leaveRequest", required = false) String leaveRequestJson,
            @RequestPart(value = "document", required = false) MultipartFile document,
            @RequestBody(required = false) String rawBody) {
        try {
            LeaveRequestDto leaveRequest;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // Handle JSON request
            if (rawBody != null) {
                try {
                    leaveRequest = objectMapper.readValue(rawBody, LeaveRequestDto.class);
                } catch (JsonProcessingException e) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid JSON format: " + e.getMessage()));
                }
            }
            // Handle multipart request
            else if (leaveRequestJson != null) {
                try {
                    leaveRequest = objectMapper.readValue(leaveRequestJson, LeaveRequestDto.class);
                } catch (JsonProcessingException e) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid JSON in leaveRequest part: " + e.getMessage()));
                }
            }
            else {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("No leave request data provided"));
            }

            // Validate required fields
            if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null ||
                leaveRequest.getReason() == null || leaveRequest.getType() == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Required fields missing: startDate, endDate, reason, and type are required"));
            }

            Long employeeId = getEmployeeIdFromUser(currentUser);
            LeaveDto result = leaveService.createLeaveRequest(employeeId, leaveRequest, document);
            return ResponseEntity.ok(result);
        } catch (LeaveAPIException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{leaveId}/status")
    @Operation(summary = "Update leave request status", 
               description = "Updates the status of a leave request (APPROVED/REJECTED). Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status or missing rejection reason"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveDto> updateLeaveStatus(
            @Parameter(description = "Leave status update details") @RequestBody LeaveStatusUpdateDto statusUpdate) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(statusUpdate.getLeaveId(), statusUpdate));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending leave requests (Admin/HR)",
               description = "Returns all pending leave requests. Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending leave requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<List<LeaveDto>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @GetMapping("/{leaveId}")
    @Operation(summary = "Get leave request by ID", 
               description = "Returns a specific leave request by its ID. Authorization checks based on role/ownership might be needed.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized to view this leave"),
        @ApiResponse(responseCode = "404", description = "Leave request not found") 
    })
    public ResponseEntity<LeaveDto> getLeaveById(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            Authentication authentication) {
                
        LeaveDto leave = leaveService.getLeaveById(leaveId);

        boolean isAdminOrHr = authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN") || 
                                         grantedAuthority.getAuthority().equals("ROLE_HR_MANAGER"));
        
        if (isAdminOrHr || (leave.getEmployee() != null && leave.getEmployee().getUser() != null && leave.getEmployee().getUser().getId().equals(currentUser.getId()))) {
            return ResponseEntity.ok(leave);
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{leaveId}")
    @Operation(summary = "Cancel leave request", 
               description = "Cancels a pending leave request. Users can only cancel their own.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel non-pending leave request"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized to cancel this leave"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<Void> cancelLeaveRequest(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) { 
        Long employeeId = getEmployeeIdFromUser(currentUser);
        leaveService.cancelLeaveRequest(leaveId, employeeId); 
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/balances")
    @Operation(summary = "Get leave balances for the current employee")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<LeaveBalanceDto>> getLeaveBalances(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", 0L));
        
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "user", user.getId()));
        
        List<LeaveBalanceDto> balances = leaveService.getEmployeeLeaveBalances(employee.getId());
        return ResponseEntity.ok(balances);
    }
    
    private Long getEmployeeIdFromUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Authenticated user cannot be null");
        }
        EmployeeDto employee = employeeService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee record not found for the authenticated user: " + user.getUsername()));
        if (employee.getId() == null) {
             throw new IllegalStateException("Found employee record has a null ID for user: " + user.getUsername());
        }
        return employee.getId();
    }

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String message;
    }
} 