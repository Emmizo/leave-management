package com.hr_management.hr.controller;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.LeaveRequestDto;
import com.hr_management.hr.model.LoginRequestDto;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "Leave request management APIs")
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeService employeeService;

    public LeaveController(LeaveService leaveService, EmployeeService employeeService) {
        this.leaveService = leaveService;
        this.employeeService = employeeService;
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
            if (principal instanceof User) {
                User currentUser = (User) principal;
                Long employeeId = getEmployeeIdFromUser(currentUser);
                return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId));
            } else {
                 System.err.println("Unexpected principal type: " + principal.getClass().getName());
                 return ResponseEntity.status(403).build();
            }
        }
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Create a new leave request", 
               description = "Creates a new leave request for the authenticated employee. Optionally include a supporting document.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or file error"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee record not found for the user")
    })
    public ResponseEntity<LeaveDto> createLeaveRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Leave request details (JSON)") 
            @Valid @RequestPart("leaveRequest") LeaveRequestDto leaveRequest,
            @Parameter(description = "Optional supporting document")
            @RequestPart(value = "document", required = false) MultipartFile document) {
        
        Long employeeId = getEmployeeIdFromUser(currentUser);
        return ResponseEntity.ok(leaveService.createLeaveRequest(employeeId, leaveRequest, document));
    }

    @PutMapping("/{leaveId}/status")
    @Operation(summary = "Update leave request status", 
               description = "Updates the status of a leave request (APPROVED/REJECTED). Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeaveDto> updateLeaveStatus(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId,
            @Parameter(description = "New status (APPROVED/REJECTED)") @RequestParam String status,
            @Parameter(description = "Reason for rejection (required if status is REJECTED)") @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(leaveId, status, rejectionReason));
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
} 