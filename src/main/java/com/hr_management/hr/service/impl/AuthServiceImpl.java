package com.hr_management.hr.service.impl;

import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.AuthService;
import com.hr_management.hr.service.EmailTemplateService;
import com.hr_management.hr.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailTemplateService emailTemplateService;

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

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);
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