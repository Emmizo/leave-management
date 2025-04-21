package com.hr_management.hr.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LoginRequestDto;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmailService;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.JwtService;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final EmployeeService employeeService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthorizedClientService clientService;

    public AuthController(
            UserService userService,
            EmployeeService employeeService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService,
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            OAuth2AuthorizedClientService clientService) {
        this.userService = userService;
        this.employeeService = employeeService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
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
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
            )
        );
        
        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UsernameNotFoundException("Principal is null after authentication");
        }
        
        if (principal instanceof User user) {
             // user variable is already cast and available here
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

    @PostMapping("/microsoft/login")
    @Operation(
        summary = "Microsoft OAuth2 Login",
        description = "Initiates Microsoft OAuth2 login flow and returns authorization URL for authentication",
        tags = {"Authentication"},
        security = @SecurityRequirement(name = ""),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authorization URL generated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        description = "Authorization response containing URL and state",
                        example = "{\"authUrl\": \"https://login.microsoftonline.com/...\", \"state\": \"random-uuid\"}"
                    )
                )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error during OAuth2 flow initiation")
        }
    )
    public ResponseEntity<Map<String, String>> microsoftLogin(HttpServletRequest request) {
        // Generate a state parameter for security
        String state = UUID.randomUUID().toString();
        
        // Store the state in the session for validation later
        HttpSession session = request.getSession();
        session.setAttribute("oauth2_state", state);
        
        // Use the configured OAuth2 properties
        String clientId = "3193b05b-c8ec-4bad-8e5a-0845d604883d";
        String redirectUri = "http://localhost:5456/api/auth/microsoft/callback";
        String scope = "openid profile email User.Read";
        
        String authorizationUrl = String.format(
            "https://login.microsoftonline.com/202c75b4-8389-4dce-b7a9-9d2c4f7b1bad/oauth2/v2.0/authorize?" +
            "client_id=%s&" +
            "redirect_uri=%s&" +
            "response_type=code&" +
            "scope=%s&" +
            "state=%s",
            clientId,
            URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
            URLEncoder.encode(scope, StandardCharsets.UTF_8),
            state
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("authorizationUrl", authorizationUrl);
        response.put("state", state);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/microsoft/callback")
    @Operation(
        summary = "Microsoft OAuth2 Callback",
        description = "Handles the callback from Microsoft OAuth2 authentication and returns JWT token with user details",
        tags = {"Authentication"},
        security = @SecurityRequirement(name = ""),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authentication successful",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<?> microsoftCallback(
            @Parameter(description = "OAuth2 callback payload containing code and state")
            @RequestBody Map<String, String> payload) {
        
        try {
            // Exchange the authorization code for tokens using OAuth2 client
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("azure", "microsoft");
            if (client == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Failed to authenticate with Microsoft"));
            }

            String accessToken = client.getAccessToken().getTokenValue();

            // Get user info from Microsoft Graph API
            String userInfoEndpoint = "https://graph.microsoft.com/v1.0/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                userInfoRequest,
                Map.class
            );

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("mail");
            String name = (String) userInfo.get("displayName");

            // Check if user exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setUsername(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole(Role.EMPLOYEE);
                user = userRepository.save(user);

                // Create employee record
                Employee employee = new Employee();
                employee.setUser(user);
                employee.setEmail(email);
                String[] nameParts = name.split(" ", 2);
                employee.setFirstName(nameParts[0]);
                employee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                employee.setDepartment("Unassigned");
                employee.setPosition("New Employee");
                employeeRepository.save(employee);
            }

            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);
            
            // Get employee details
            EmployeeDto employeeDto = employeeService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .user(employeeDto)
                    .build());

        } catch (Exception e) {
            logger.error("Error during Microsoft OAuth callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process Microsoft OAuth callback"));
        }
    }
}