package com.hr_management.controller;

import com.hr_management.entity.Leave;
import com.hr_management.entity.LeaveRequest;
import com.hr_management.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "Leave request management APIs")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Create a new leave request", description = "Creates a new leave request for the authenticated employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Leave> createLeaveRequest(
            @Parameter(hidden = true) @AuthenticationPrincipal Long employeeId,
            @Parameter(description = "Leave request details") @Valid @RequestBody LeaveRequest leaveRequest) {
        return ResponseEntity.ok(leaveService.createLeaveRequest(employeeId, leaveRequest));
    }

    @PutMapping("/{leaveId}/status")
    @Operation(summary = "Update leave request status", description = "Updates the status of a leave request (APPROVED/REJECTED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<Leave> updateLeaveStatus(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId,
            @Parameter(description = "New status (APPROVED/REJECTED)") @RequestParam String status,
            @Parameter(description = "Reason for rejection (required if status is REJECTED)") @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(leaveId, status, rejectionReason));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee's leave requests", description = "Returns all leave requests for the authenticated employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<Leave>> getEmployeeLeaves(
            @Parameter(hidden = true) @AuthenticationPrincipal Long employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending leave requests", description = "Returns all pending leave requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending leave requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<Leave>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @GetMapping("/{leaveId}")
    @Operation(summary = "Get leave request by ID", description = "Returns a specific leave request by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<Leave> getLeaveById(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId) {
        return ResponseEntity.ok(leaveService.getLeaveById(leaveId));
    }

    @DeleteMapping("/{leaveId}")
    @Operation(summary = "Cancel leave request", description = "Cancels a pending leave request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave request cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel non-pending leave request"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<Void> cancelLeaveRequest(
            @Parameter(description = "ID of the leave request") @PathVariable Long leaveId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long employeeId) {
        leaveService.cancelLeaveRequest(leaveId, employeeId);
        return ResponseEntity.ok().build();
    }
} 