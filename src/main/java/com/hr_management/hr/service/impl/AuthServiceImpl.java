package com.hr_management.hr.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.ForgotPasswordRequestDto;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.ResetPasswordRequestDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.AuthService;
import com.hr_management.hr.service.HtmlEmailTemplateService;
import com.hr_management.hr.service.JwtService;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HtmlEmailTemplateService htmlEmailTemplateService;

    public AuthServiceImpl(UserRepository userRepository, 
                         EmployeeRepository employeeRepository,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         HtmlEmailTemplateService htmlEmailTemplateService) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.htmlEmailTemplateService = htmlEmailTemplateService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequestDto request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        try {
            // Generate a random password
            String randomPassword = generateRandomPassword();
            
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setEmail(request.getEmail());
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            user.setEnabled(true);
            user.setProfilePicture(""); // Set default empty string
            user = userRepository.save(user);

            // Create employee record
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setFirstName(request.getFirstName());
            employee.setLastName(request.getLastName());
            employee.setDepartment(request.getDepartment());
            employee.setPosition(request.getPosition());
            employee.setPhone(request.getPhone());
            employee.setEmail(request.getEmail());
            employee.setGender(request.getGender());
            employee.setMicrosoftId(""); // Set default empty string
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
                    .gender(employee.getGender())
                    .build();

            // Send welcome email with the random password
            htmlEmailTemplateService.sendWelcomeEmail(user, employee, randomPassword);

            return new AuthResponse(token, employeeDto, user.getProfilePicture());
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage());
            throw new RuntimeException("Error during registration: " + e.getMessage());
        }
    }

    /**
     * Generates a random password that meets security requirements
     * @return A secure random password
     */
    private String generateRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        // Ensure at least one of each character type
        StringBuilder password = new StringBuilder();
        password.append(upperCase.charAt((int)(Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int)(Math.random() * lowerCase.length())));
        password.append(numbers.charAt((int)(Math.random() * numbers.length())));
        password.append(specialChars.charAt((int)(Math.random() * specialChars.length())));
        
        // Add additional random characters to make it 12 characters long
        String allChars = upperCase + lowerCase + numbers + specialChars;
        for (int i = 0; i < 8; i++) {
            password.append(allChars.charAt((int)(Math.random() * allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int)(Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        log.info("Processing direct password reset for email: {}", email);
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            log.error("Invalid new password provided");
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", 0L);
                });

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getUsername());

        // Send password reset confirmation email
        htmlEmailTemplateService.sendPasswordResetConfirmationEmail(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDto request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", request.getEmail());
                    return new ResourceNotFoundException("User", "email", 0L);
                });

        // Generate a new reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiryDate(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        log.info("Reset token generated for user: {}", user.getUsername());

        // Send password reset email with link
        htmlEmailTemplateService.sendPasswordResetEmail(user, resetToken);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    public void resetPasswordWithToken(ResetPasswordRequestDto request) {
        log.info("Processing password reset with token");
        
        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            log.error("Invalid new password provided");
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        // Find user by reset token
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> {
                    log.error("Invalid reset token provided");
                    return new ResourceNotFoundException("User", "reset token", 0L);
                });

        // Validate token expiry
        if (user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            log.error("Reset token expired for user: {}", user.getUsername());
            throw new RuntimeException("Reset token has expired");
        }

        // Log the raw password before encoding (for debugging only)
        log.debug("Raw new password length: {}", request.getNewPassword().length());
        
        // Update password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        log.debug("Password encoded successfully");
        
        user.setPassword(encodedPassword);
        // Clear the reset token
        user.setResetToken(null);
        user.setResetTokenExpiryDate(null);
        userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getUsername());

        // Send password reset confirmation email
        htmlEmailTemplateService.sendPasswordResetConfirmationEmail(user);
    }
} 