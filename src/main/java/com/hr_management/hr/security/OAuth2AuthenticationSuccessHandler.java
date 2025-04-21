package com.hr_management.hr.security;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserRepository userRepository, EmployeeRepository employeeRepository, EmployeeService employeeService, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        log.info("OAuth2 Authentication successful. Preparing JWT and redirect.");

        // Extract user details from OAuth2User
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Extract all available attributes
        String email = oAuth2User.getAttribute("email");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        String fullName = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String microsoftId = oAuth2User.getAttribute("oid");
        String preferredUsername = oAuth2User.getAttribute("preferred_username");
        
        // Log all available attributes for debugging
        log.debug("OAuth2 User Attributes:");
        oAuth2User.getAttributes().forEach((key, value) -> 
            log.debug("{}: {}", key, value));
        
        if (email == null) {
            // Try preferred_username as fallback
            email = preferredUsername;
            if (email == null) {
                log.error("Email not found in OAuth2 user attributes");
                String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                        .queryParam("error", "EmailNotFound")
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }
        }
        
        log.debug("Extracted email '{}' from OAuth2 user.", email);
        
        // Find or create user
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        Employee employee;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.debug("Found existing user with email '{}'.", email);
            
            // Update user information if needed
            if (picture != null && (user.getProfilePicture() == null || user.getProfilePicture().isEmpty())) {
                user.setProfilePicture(picture);
            }
            
            // Set Microsoft ID if available and not set
            if (microsoftId != null && (user.getMicrosoftId() == null || user.getMicrosoftId().isEmpty())) {
                user.setMicrosoftId(microsoftId);
            }
            
            user = userRepository.save(user);
            
            // Update or create employee record
            Optional<Employee> employeeOptional = employeeRepository.findByUser(user);
            if (employeeOptional.isPresent()) {
                employee = employeeOptional.get();
                updateEmployeeInfo(employee, givenName, familyName, fullName, microsoftId);
            } else {
                employee = createEmployee(user, givenName, familyName, fullName, microsoftId);
            }
        } else {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setUsername(email); // Use email as username
            user.setPassword(passwordEncoder.encode("OAUTH2_USER")); // Placeholder password for OAuth2 users
            user.setRole(Role.USER);
            user.setEnabled(true);
            user.setProfilePicture(picture);
            user.setMicrosoftId(microsoftId);
            
            user = userRepository.save(user);
            log.debug("Created new user with email '{}'.", email);
            
            // Create employee record
            employee = createEmployee(user, givenName, familyName, fullName, microsoftId);
        }
        
        // Generate token using the user's username (email) as the subject
        String token = jwtService.generateToken(user);
        log.debug("Generated JWT token for user '{}'.", email);
        
        // Create EmployeeDto for response
        EmployeeDto employeeDto = EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .microsoftId(employee.getMicrosoftId())
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
        
        // Create AuthResponse
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(employeeDto)
                .build();
        
        // Check if this is a direct API call or a redirect
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.contains("/api/auth/microsoft/login")) {
            // Return JSON response for API calls
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), authResponse);
            return;
        }
        
        // For regular OAuth2 flow, redirect with token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("firstName", employee.getFirstName())
                .queryParam("lastName", employee.getLastName())
                .queryParam("email", user.getEmail())
                .queryParam("picture", user.getProfilePicture() != null ? user.getProfilePicture() : "")
                .build().toUriString();
        
        log.info("Redirecting user '{}' to: {}", email, targetUrl);
        
        // Configure the redirect strategy
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private void updateEmployeeInfo(Employee employee, String givenName, String familyName, String fullName, String microsoftId) {
        // Update first name if available and not set
        if (givenName != null && (employee.getFirstName() == null || employee.getFirstName().isEmpty())) {
            employee.setFirstName(givenName);
        }
        
        // Update last name if available and not set
        if (familyName != null && (employee.getLastName() == null || employee.getLastName().isEmpty())) {
            employee.setLastName(familyName);
        }
        
        // If neither given_name nor family_name is available, try to split full name
        if ((givenName == null || familyName == null) && fullName != null) {
            String[] nameParts = fullName.split(" ", 2);
            if (givenName == null && nameParts.length > 0) {
                employee.setFirstName(nameParts[0]);
            }
            if (familyName == null && nameParts.length > 1) {
                employee.setLastName(nameParts[1]);
            }
        }
        
        // Update Microsoft ID if available and not set
        if (microsoftId != null && (employee.getMicrosoftId() == null || employee.getMicrosoftId().isEmpty())) {
            employee.setMicrosoftId(microsoftId);
        }
        
        employeeRepository.save(employee);
        log.debug("Updated employee record for user '{}'.", employee.getEmail());
    }
    
    private Employee createEmployee(User user, String givenName, String familyName, String fullName, String microsoftId) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmail(user.getEmail());
        
        // Set first name
        if (givenName != null) {
            employee.setFirstName(givenName);
        } else if (fullName != null) {
            String[] nameParts = fullName.split(" ", 2);
            employee.setFirstName(nameParts[0]);
        } else {
            employee.setFirstName("Not Set");
        }
        
        // Set last name
        if (familyName != null) {
            employee.setLastName(familyName);
        } else if (fullName != null) {
            String[] nameParts = fullName.split(" ", 2);
            if (nameParts.length > 1) {
                employee.setLastName(nameParts[1]);
            } else {
                employee.setLastName("Not Set");
            }
        } else {
            employee.setLastName("Not Set");
        }
        
        // Set default values for required fields
        employee.setDepartment("Not Applicable");
        employee.setPosition("Not Applicable");
        employee.setAnnualLeaveBalance(20); // Default annual leave balance
        
        if (microsoftId != null) {
            employee.setMicrosoftId(microsoftId);
        }
        
        employee = employeeRepository.save(employee);
        log.debug("Created new employee record for user '{}'.", user.getEmail());
        return employee;
    }
} 