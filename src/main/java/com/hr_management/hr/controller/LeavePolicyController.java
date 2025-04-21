package com.hr_management.hr.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.entity.LeavePolicy;
import com.hr_management.hr.service.LeavePolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/leave-policies")
@Tag(name = "Leave Policy Management", description = "APIs for managing leave policies")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    public LeavePolicyController(LeavePolicyService leavePolicyService) {
        this.leavePolicyService = leavePolicyService;
    }

    @GetMapping
    @Operation(summary = "Get all leave policies", 
               description = "Retrieves a list of all leave policies. Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave policies retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<List<LeavePolicy>> getAllLeavePolicies() {
        return ResponseEntity.ok(leavePolicyService.getAllLeavePolicies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave policy by ID", 
               description = "Retrieves a specific leave policy by its ID. Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave policy retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "Leave policy not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeavePolicy> getLeavePolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(leavePolicyService.getLeavePolicyById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new leave policy", 
               description = "Creates a new leave policy. Requires ADMIN role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave policy created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeavePolicy> createLeavePolicy(@RequestBody LeavePolicy leavePolicy) {
        return ResponseEntity.ok(leavePolicyService.createLeavePolicy(leavePolicy));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a leave policy", 
               description = "Updates an existing leave policy. Requires ADMIN role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave policy updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "Leave policy not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<LeavePolicy> updateLeavePolicy(
            @PathVariable Long id, 
            @RequestBody LeavePolicy leavePolicy) {
        return ResponseEntity.ok(leavePolicyService.updateLeavePolicy(id, leavePolicy));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a leave policy", 
               description = "Deletes a leave policy. Requires ADMIN role.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave policy deleted successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "Leave policy not found")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<Void> deleteLeavePolicy(@PathVariable Long id) {
        leavePolicyService.deleteLeavePolicy(id);
        return ResponseEntity.ok().build();
    }
} 