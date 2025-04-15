package com.hr_management.controller;

import com.hr_management.entity.Employee;
import com.hr_management.entity.User;
import com.hr_management.service.EmployeeService;
import com.hr_management.service.JwtService;
import com.hr_management.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;
    private final EmployeeService employeeService;
    private final JwtService jwtService;

    public AuthController(UserService userService, EmployeeService employeeService, JwtService jwtService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and employee", description = "Creates a new user account and associated employee record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<User> register(
            @Parameter(description = "Username for the new account") @RequestParam String username,
            @Parameter(description = "Password for the new account") @RequestParam String password,
            @Parameter(description = "Email address for the new account") @RequestParam String email,
            @Parameter(description = "First name of the employee") @RequestParam String firstName,
            @Parameter(description = "Last name of the employee") @RequestParam String lastName,
            @Parameter(description = "Department of the employee") @RequestParam String department,
            @Parameter(description = "Position of the employee") @RequestParam String position) {
        
        User user = userService.createUser(username, password, email);
        
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setDepartment(department);
        employee.setPosition(position);
        employeeService.save(employee);
        
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<Map<String, String>> login(
            @Parameter(description = "Username") @RequestParam String username,
            @Parameter(description = "Password") @RequestParam String password) {
        
        User user = userService.authenticate(username, password);
        String token = jwtService.generateToken(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user details", description = "Returns the details of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<Employee> getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Employee employee = employeeService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return ResponseEntity.ok(employee);
    }
} 