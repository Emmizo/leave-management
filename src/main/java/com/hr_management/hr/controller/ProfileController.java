package com.hr_management.hr.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.ErrorResponse;
import com.hr_management.hr.model.MessageResponse;
import com.hr_management.hr.model.ProfileUpdateDto;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile Management", description = "Profile management APIs")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public ProfileController(
            UserService userService,
            FileStorageService fileStorageService,
            UserRepository userRepository,
            RestTemplate restTemplate) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @PutMapping
    @Operation(summary = "Update user profile", 
               description = "Updates the user's profile information",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateDto request) {
        try {
            User user = (User) authentication.getPrincipal();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User not authenticated"));
            }

            // Use UserService to update profile
            userService.updateProfile(user.getId(), request);

            return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update profile picture", 
               description = "Updates the user's profile picture",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "415", description = "Unsupported media type")
    })
    public ResponseEntity<?> updateProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Received file upload request");
            System.out.println("Content-Type: " + file.getContentType());
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());

            User user = (User) authentication.getPrincipal();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User not authenticated"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File is required and cannot be empty"));
        }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Only image files are allowed. Received content type: " + contentType));
            }

            // Store the file and get the path
            String filePath = fileStorageService.storeFile(file, "profile_" + user.getId());
            
            // Create a minimal profile update DTO with just the profile picture path
            ProfileUpdateDto profileUpdate = new ProfileUpdateDto();
            profileUpdate.setProfilePicture(filePath);
            
            // Update the user's profile
            userService.updateProfile(user.getId(), profileUpdate);

            return ResponseEntity.ok(new MessageResponse("Profile picture updated successfully"));
        } catch (IOException e) {
            System.out.println("Error storing file: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to store profile picture: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("Runtime error: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{userId}/photo")
    @Operation(summary = "Get user profile picture", 
               description = "Retrieves the profile picture for a user. For Microsoft users, this will proxy the request to Microsoft Graph API.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "User not authorized"),
        @ApiResponse(responseCode = "404", description = "User not found or no profile picture available")
    })
    public ResponseEntity<byte[]> getProfilePicture(
            @PathVariable String userId,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            logger.info("Attempting to fetch profile picture for user ID: {}", userId);
            
            // Get the current authenticated user
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                logger.warn("Unauthorized access attempt - no authenticated user");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Find the target user by Microsoft ID
            User targetUser = userRepository.findByMicrosoftId(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found with Microsoft ID: {}", userId);
                        return new RuntimeException("User not found");
                    });

            logger.info("Found target user: {}", targetUser.getEmail());

            // Only allow access if the current user is the same as the target user or is an admin
            if (!currentUser.getId().equals(targetUser.getId()) && 
                !currentUser.getRole().equals(Role.ADMIN)) {
                logger.warn("Forbidden access attempt - user {} trying to access {}'s photo", 
                    currentUser.getEmail(), targetUser.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (targetUser.getMicrosoftId() == null) {
                logger.warn("User {} has no Microsoft ID", targetUser.getEmail());
                return ResponseEntity.notFound().build();
            }

            if (targetUser.getAccessToken() == null) {
                logger.warn("User {} has no access token", targetUser.getEmail());
                return ResponseEntity.notFound().build();
            }

            // Construct Microsoft Graph API URL for the photo
            String photoUrl = String.format("https://graph.microsoft.com/v1.0/users/%s/photo/$value", targetUser.getMicrosoftId());
            logger.info("Fetching photo from Microsoft Graph API: {}", photoUrl);

            // Make request to Microsoft Graph API
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(targetUser.getAccessToken());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                photoUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully retrieved profile picture for user: {}", targetUser.getEmail());
                // Set appropriate content type
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.IMAGE_JPEG);
                return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
            } else {
                logger.warn("Failed to retrieve profile picture from Microsoft Graph API. Status: {}", response.getStatusCode());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving profile picture: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
} 