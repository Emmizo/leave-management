package com.hr_management.hr.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.AuthService;
import com.hr_management.hr.service.EmailTemplateService;
import com.hr_management.hr.service.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailTemplateService emailTemplateService;

    public AuthServiceImpl(UserRepository userRepository, 
                          EmployeeRepository employeeRepository, 
                          PasswordEncoder passwordEncoder, 
                          JwtService jwtService, 
                          EmailTemplateService emailTemplateService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailTemplateService = emailTemplateService;
    }

    @Override
    public AuthResponse register(RegisterRequestDto request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setEnabled(true);
        userRepository.save(user);

        // Create employee record
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employeeRepository.save(employee);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Create EmployeeDto for response
        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();

        return AuthResponse.builder()
                .token(token)
                .user(employeeDto)
                .build();
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", 0L));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        emailTemplateService.sendWelcomeEmail(user, userRepository.findById(user.getId()).get().getEmployee());
    }
} 