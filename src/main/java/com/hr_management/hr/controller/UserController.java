package com.hr_management.hr.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public UserController(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') || hasRole('HR_MANAGER')")
    @Operation(summary = "Get all users with positions", 
               description = "Retrieves a list of all users with their positions",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<UserDto> userDtos = users.stream()
            .map(user -> {
                String position = employeeRepository.findByUser(user)
                    .map(employee -> employee.getPosition())
                    .orElse("Not assigned");
                
                return UserDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .provider(user.getProvider())
                    .providerId(user.getProviderId())
                    .enabled(user.isEnabled())
                    .build();
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN') || hasRole('HR_MANAGER')")
    @Operation(summary = "Update user active status", 
               description = "Updates whether a user is active or inactive",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("enabled field is required in request body");
        }

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        String position = employeeRepository.findByUser(updatedUser)
                .map(employee -> employee.getPosition())
                .orElse("Not assigned");

        UserDto userDto = UserDto.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole().name())
                .provider(updatedUser.getProvider())
                .providerId(updatedUser.getProviderId())
                .enabled(updatedUser.isEnabled())
                .build();

        return ResponseEntity.ok(userDto);
    }
} 