package com.hr_management.hr.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.ProfileUpdateDto;
import com.hr_management.hr.security.CustomUserDetails;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile Management", description = "APIs for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    @Operation(summary = "Update user profile", 
              description = "Updates both user and employee information for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
        @ApiResponse(responseCode = "404", description = "User or employee not found")
    })
    public ResponseEntity<User> updateProfile(
            @Parameter(description = "Profile update data", required = true)
            @RequestBody ProfileUpdateDto profileUpdateDto) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return ResponseEntity.status(401).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User updatedUser = userService.updateProfile(userDetails.getId(), profileUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }
} 