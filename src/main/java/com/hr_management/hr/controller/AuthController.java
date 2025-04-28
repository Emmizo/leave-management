package com.hr_management.hr.controller;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.ChangePasswordRequestDto;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.ErrorResponse;
import com.hr_management.hr.model.ForgotPasswordRequestDto;
import com.hr_management.hr.model.LoginRequestDto;
import com.hr_management.hr.model.MessageResponse;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.ResetPasswordRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.AuthService;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.HtmlEmailTemplateService;
import com.hr_management.hr.service.JwtService;
import com.hr_management.hr.service.MicrosoftAuthService;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final UserService userService;
    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final HtmlEmailTemplateService htmlEmailTemplateService;
    private final AuthService authService;
    private final MicrosoftAuthService microsoftAuthService;
    private final OAuth2AuthorizedClientService clientService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(
            UserService userService,
            EmployeeService employeeService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            HtmlEmailTemplateService htmlEmailTemplateService,
            AuthService authService,
            MicrosoftAuthService microsoftAuthService,
            OAuth2AuthorizedClientService clientService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.htmlEmailTemplateService = htmlEmailTemplateService;
        this.authService = authService;
        this.microsoftAuthService = microsoftAuthService;
        this.clientService = clientService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", 
               description = "Authenticates user and returns JWT token with user details. This endpoint is publicly accessible and does not require authentication.",
               security = {}) // Empty security array means no security required
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequestDto loginRequest) {
        
        logger.info("Attempting login for identifier: {}", loginRequest.getUsername());
        
        try {
            // First try to find user by username or email
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(loginRequest.getUsername())
                            .orElseThrow(() -> {
                                logger.error("User not found with identifier: {}", loginRequest.getUsername());
                                return new UsernameNotFoundException("Invalid username or email");
                            }));

            logger.info("User found: {}", user.getUsername());
            
            // Authenticate with the found user's username
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    user.getUsername(), // Use the actual username for authentication
                    loginRequest.getPassword()
                )
            );
            
            logger.info("Authentication successful for user: {}", user.getUsername());
            
            Object principal = authentication.getPrincipal();
            if (principal == null) {
                logger.error("Principal is null after authentication");
                throw new UsernameNotFoundException("Principal is null after authentication");
            }
            
            if (principal instanceof User authenticatedUser) {
                logger.info("User authenticated: {}", authenticatedUser.getUsername());
            } else {
                logger.error("Unexpected principal type: {}", principal.getClass());
                throw new UsernameNotFoundException("Unexpected principal type after authentication: " + principal.getClass());
            }

            String token = jwtService.generateToken(user);
            logger.info("JWT token generated for user: {}", user.getUsername());
            
            EmployeeDto employeeDto = employeeService.findByUser(user)
                    .orElseThrow(() -> {
                        logger.error("Employee record not found for user: {}", user.getUsername());
                        return new RuntimeException("Employee record not found for authenticated user: " + user.getUsername());
                    });

            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .user(employeeDto)
                    .profilePicture(user.getProfilePicture())
                    .build());
        } catch (RuntimeException e) {
            logger.error("Login failed for identifier {}: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", 
               description = "Creates a new user account and associated employee record. Sends welcome email.",
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
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequestDto registerRequest) {
        
        // Generate a random password regardless of whether one was provided
        String randomPassword = generateRandomPassword();
        
        User user = userService.createUser(
            registerRequest.getUsername(), 
            randomPassword, 
            registerRequest.getEmail()
        );
        
        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .department(registerRequest.getDepartment())
                .position(registerRequest.getPosition())
                .email(registerRequest.getEmail())
                .gender(registerRequest.getGender())
                .user(UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .build();
        
        EmployeeDto savedEmployee = employeeService.save(employeeDto);
        String token = jwtService.generateToken(user);

        // Get the employee entity to pass to the email template service
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found for user: " + user.getUsername()));
        
        // Send welcome email using the HTML template service with the random password
        htmlEmailTemplateService.sendWelcomeEmail(user, employee, randomPassword);
        
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(savedEmployee)
                .build());
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
        if (employee.getUser() != null) {
            employee.getUser().setProfilePicture(user.getProfilePicture());
        }
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/microsoft/login")
    @Operation(summary = "Initiate Microsoft login", 
               description = "Initiates the Microsoft OAuth login flow. Returns the authorization URL to redirect the user to Microsoft's login page.",
               security = {}) // No security required
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authorization URL generated successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> microsoftLogin() {
        String state = UUID.randomUUID().toString();
        String authorizationUrl = microsoftAuthService.getAuthorizationUrl(state);
        return ResponseEntity.ok(authorizationUrl);
    }

    @PostMapping("/microsoft/callback")
    public ResponseEntity<?> microsoftCallback(@RequestBody Map<String, String> params) {
        try {
            String code = params.get("code");
            // String state = params.get("state");
            Map<String, Object> tokenResponse = microsoftAuthService.exchangeCodeForTokens(code);
            String accessToken = (String) tokenResponse.get("access_token");
            Map<String, Object> userInfo = microsoftAuthService.getUserInfo(accessToken);
            userInfo.put("access_token", accessToken); // Ensure access token is available for profile picture download
            User user = microsoftAuthService.createOrUpdateUser(userInfo);
            EmployeeDto employeeDto = employeeService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Employee record not found"));
            String jwtToken = jwtService.generateToken(user);
            
            // Update the nested user object with the profile picture URL
            if (employeeDto.getUser() != null) {
                employeeDto.getUser().setProfilePicture(user.getProfilePicture());
            }
            
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .user(employeeDto)
                    .profilePicture(user.getProfilePicture())
                    .build());
        } catch (Exception e) {
            logger.error("Error during Microsoft authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to authenticate with Microsoft"));
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", 
               description = "Sends a password reset email to the user's email address",
               security = {}) // No security required
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided email")
    })
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", 
               description = "Resets the user's password using the token received via email",
               security = {}) // No security required
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token or password"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        
        authService.resetPasswordWithToken(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", 
               description = "Change user's password after verifying current password",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or current password is incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token")
    })
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        try {
            User user = (User) authentication.getPrincipal();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User not authenticated"));
            }

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Current password is incorrect"));
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/update-profile-picture")
    @Operation(summary = "Update Microsoft profile picture", 
               description = "Updates the profile picture URL for a Microsoft-authenticated user",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
        @ApiResponse(responseCode = "400", description = "User is not a Microsoft user"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<UserDto> updateProfilePicture(
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getMicrosoftId() == null) {
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = microsoftAuthService.updateProfilePicture(user);
        UserDto userDto = UserDto.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole().name())
                .profilePicture(updatedUser.getProfilePicture())
                .build();

        return ResponseEntity.ok(userDto);
    }
}