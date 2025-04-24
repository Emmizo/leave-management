package com.hr_management.hr.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.ErrorResponse;
import com.hr_management.hr.model.MessageResponse;
import com.hr_management.hr.model.ProfileUpdateDto;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile Management", description = "Profile management APIs")
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ProfileController(
            UserService userService,
            FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
} 