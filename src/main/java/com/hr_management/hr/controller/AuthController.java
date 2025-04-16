package com.hr_management.hr.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.LoginRequestDto;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.security.JwtService;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.UserService;
import com.hr_management.hr.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;
    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthController(
            UserService userService,
            EmployeeService employeeService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", 
               description = "Authenticates user and returns JWT token with user details",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "User credentials for login",
                   required = true,
                   content = @Content(
                       mediaType = "application/json",
                       schema = @Schema(implementation = LoginRequestDto.class)
                   )
               )
              )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequestDto loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );
        
        Object principal = authentication.getPrincipal();
        User user;
        if (principal instanceof User) {
             user = (User) principal;
        } else {
             throw new UsernameNotFoundException("Unexpected principal type after authentication: " + principal.getClass());
        }

        String token = jwtService.generateToken(user);
        
        EmployeeDto employeeDto = employeeService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee record not found for authenticated user: " + user.getUsername()));

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(employeeDto)
                .build());
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and employee (Admin/HR)", 
               description = "Creates a new user account and associated employee record. Sends welcome email. Requires ADMIN or HR_MANAGER role.",
               security = @SecurityRequirement(name = "bearerAuth"),
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "Details for registering a new user and employee",
                   required = true,
                   content = @Content(
                       mediaType = "application/json",
                       schema = @Schema(implementation = RegisterRequestDto.class)
                   )
               )
              )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input (check validation errors)"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized (Requires Admin/HR Manager)"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequestDto registerRequest) {
        
        User user = userService.createUser(
            registerRequest.getUsername(), 
            registerRequest.getPassword(), 
            registerRequest.getEmail()
        );
        
        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .department(registerRequest.getDepartment())
                .position(registerRequest.getPosition())
                .email(registerRequest.getEmail())
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
        
        EmployeeDto savedEmployee = employeeService.save(employeeDto);
        String token = jwtService.generateToken(user);

        String subject = "Welcome to the HR Management System!";
        String text = String.format(
            "Hello %s,\n\nAn account has been created for you.\nUsername: %s\n\nPlease log in and consider changing your password.\n\nRegards,\nThe Admin Team",
            savedEmployee.getFirstName(),
            user.getUsername()
        );
        if (user.getEmail() != null) {
             emailService.sendSimpleMessage(user.getEmail(), subject, text);
        } else {
            System.err.println("Warning: Newly created user " + user.getUsername() + " has no email. Cannot send welcome notification.");
        }
        
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(savedEmployee)
                .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user details", 
                description = "Returns the details of the currently authenticated user",
                security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDto> getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        EmployeeDto employee = employeeService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return ResponseEntity.ok(employee);
    }
}