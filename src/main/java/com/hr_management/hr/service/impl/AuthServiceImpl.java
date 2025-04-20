package com.hr_management.hr.service.impl;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.RegisterRequestDto;
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
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .build();

        // Create employee record
        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .department(request.getDepartment())
                .position(request.getPosition())
                .user(user)
                .build();
        
        user.setEmployee(employee);

        // Save both user and employee
        userRepository.save(user);
        employeeRepository.save(employee);

        // Send welcome email
        emailTemplateService.sendWelcomeEmail(user, employee);

        // Create UserDetails object for JWT token generation
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder()
                .token(jwtToken)
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