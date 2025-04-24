package com.hr_management.hr.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.hr_management.hr.config.ApplicationProperties;
import com.hr_management.hr.config.MicrosoftOAuthProperties;
import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.enums.Gender;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.MicrosoftAuthService;

@Service
public class MicrosoftAuthServiceImpl implements MicrosoftAuthService {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftAuthServiceImpl.class);

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final MicrosoftOAuthProperties microsoftProperties;
    private final ApplicationProperties appProperties;

    public MicrosoftAuthServiceImpl(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            RestTemplate restTemplate,
            MicrosoftOAuthProperties microsoftProperties,
            ApplicationProperties appProperties) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.microsoftProperties = microsoftProperties;
        this.appProperties = appProperties;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        String scope = "openid profile email User.Read";
        return String.format(
            "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?" +
            "client_id=%s&" +
            "redirect_uri=%s&" +
            "response_type=code&" +
            "scope=%s&" +
            "state=%s",
            microsoftProperties.getTenantId(),
            microsoftProperties.getClientId(),
            URLEncoder.encode(microsoftProperties.getRedirectUri(), StandardCharsets.UTF_8),
            URLEncoder.encode(scope, StandardCharsets.UTF_8),
            state
        );
    }

    @Override
    public Map<String, Object> exchangeCodeForTokens(String code) {
        String tokenUrl = String.format(
            "https://login.microsoftonline.com/%s/oauth2/v2.0/token",
            microsoftProperties.getTenantId()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        String body = String.format(
            "client_id=%s&" +
            "scope=openid profile email User.Read&" +
            "code=%s&" +
            "redirect_uri=%s&" +
            "grant_type=authorization_code&" +
            "client_secret=%s",
            microsoftProperties.getClientId(),
            code,
            URLEncoder.encode(microsoftProperties.getRedirectUri(), StandardCharsets.UTF_8),
            microsoftProperties.getClientSecret()
        );

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            request,
            Map.class
        );

        return response.getBody();
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        // Get basic user info
        String userInfoEndpoint = "https://graph.microsoft.com/v1.0/me?$select=id,displayName,givenName,surname,mail,userPrincipalName,gender";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            userInfoEndpoint,
            HttpMethod.GET,
            request,
            Map.class
        );

        Map<String, Object> userInfo = response.getBody();
        
        // Get photo URL
        String userId = (String) userInfo.get("id");
        String photoUrl = String.format("https://graph.microsoft.com/v1.0/users/%s/photo/$value", userId);
        
        try {
            // Try to access the photo to verify it exists
            HttpEntity<?> photoRequest = new HttpEntity<>(headers);
            ResponseEntity<byte[]> photoResponse = restTemplate.exchange(
                photoUrl,
                HttpMethod.GET,
                photoRequest,
                byte[].class
            );
            
            // If we get here, the photo exists - store the full URL for our proxy endpoint
            userInfo.put("photoUrl", String.format("%s/api/users/%s/photo", appProperties.getBaseUrl(), userId));
            logger.info("Successfully verified profile picture for user: {} with ID: {}", userInfo.get("userPrincipalName"), userId);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.warn("Could not fetch photo URL: {}", e.getMessage());
            // Set a default empty string if photo URL cannot be fetched
            userInfo.put("photoUrl", "");
        }

        return userInfo;
    }

    @Override
    public User createOrUpdateUser(Map<String, Object> userInfo) {
        String email = (String) userInfo.get("mail");
        // If email is null, try userPrincipalName as a fallback
        if (email == null) {
            email = (String) userInfo.get("userPrincipalName");
        }
        String name = (String) userInfo.get("displayName");
        String firstName = (String) userInfo.get("givenName");
        String lastName = (String) userInfo.get("surname");
        String microsoftUserId = (String) userInfo.get("id");
        String gender = (String) userInfo.get("gender");
        String profilePictureUrl = (String) userInfo.get("photoUrl");
        String accessToken = (String) userInfo.get("access_token");

        if (email == null) {
            logger.error("Could not determine email (mail or userPrincipalName) from Microsoft user info: {}", userInfo);
            throw new IllegalArgumentException("Email could not be determined from Microsoft user info.");
        }
        if (microsoftUserId == null) {
            logger.error("Could not determine user ID (id) from Microsoft user info: {}", userInfo);
            throw new IllegalArgumentException("Microsoft User ID (id) could not be determined from Microsoft user info.");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("Found existing user for Microsoft sign-in: {}", email);
            // Always update profile picture for Microsoft users
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                user.setProfilePicture(profilePictureUrl);
            }
            // Store the access token
            user.setAccessToken(accessToken);
            user = userRepository.save(user);
            logger.info("Updated profile picture for user: {} with URL: {}", email, profilePictureUrl);
        } else {
            logger.info("Creating new user for Microsoft sign-in: {}", email);
            user = new User();
            user.setEmail(email);
            user.setUsername(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole(Role.EMPLOYEE);
            // Set profile picture URL for new users if available
            user.setProfilePicture(profilePictureUrl != null ? profilePictureUrl : "");
            user = userRepository.save(user);
            logger.info("Created new user with profile picture: {} with URL: {}", email, profilePictureUrl);
        }

        // Find or create employee record
        Optional<Employee> existingEmployee = employeeRepository.findByUser(user);
        Employee employee;
        if (existingEmployee.isPresent()) {
            employee = existingEmployee.get();
            logger.info("Found existing employee record for user: {}", email);
            if (employee.getMicrosoftId() == null) {
                employee.setMicrosoftId(microsoftUserId);
            }
            // Update name fields if they are null or empty
            if (employee.getFirstName() == null || employee.getFirstName().isEmpty()) {
                employee.setFirstName(firstName != null ? firstName : (name != null ? name.split(" ")[0] : "Unknown"));
            }
            if (employee.getLastName() == null || employee.getLastName().isEmpty()) {
                employee.setLastName(lastName != null ? lastName : (name != null && name.split(" ").length > 1 ? name.split(" ")[1] : "User"));
            }
            // Update gender if it's null or empty
            if (employee.getGender() == null && gender != null) {
                try {
                    employee.setGender(Gender.valueOf(gender.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid gender value from Microsoft: {}", gender);
                }
            }
        } else {
            logger.info("Creating new employee record for user: {}", email);
            employee = new Employee();
            employee.setUser(user);
            employee.setEmail(email);
            // Use Microsoft Graph data for names, with fallback to displayName
            employee.setFirstName(firstName != null ? firstName : (name != null ? name.split(" ")[0] : "Unknown"));
            employee.setLastName(lastName != null ? lastName : (name != null && name.split(" ").length > 1 ? name.split(" ")[1] : "User"));
            employee.setDepartment("Unassigned");
            employee.setPosition("New Employee");
            // Set gender from Microsoft data if available
            if (gender != null) {
                try {
                    employee.setGender(Gender.valueOf(gender.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid gender value from Microsoft: {}", gender);
                    employee.setGender(Gender.OTHER);
                }
            } else {
                employee.setGender(Gender.OTHER);
            }
            employee.setMicrosoftId(microsoftUserId);
        }
        
        employeeRepository.save(employee);

        logger.info("Successfully processed user and employee record for Microsoft sign-in: {} with profile picture: {}", email, user.getProfilePicture());
        return user;
    }

    @Override
    public User updateProfilePicture(User user) {
        if (user == null || user.getMicrosoftId() == null) {
            throw new IllegalArgumentException("User or Microsoft ID cannot be null");
        }

        String profilePictureUrl = String.format("%s/api/users/%s/photo", appProperties.getBaseUrl(), user.getMicrosoftId());
        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }
} 