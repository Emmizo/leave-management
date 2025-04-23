package com.hr_management.hr.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.ErrorResponse;
import com.hr_management.hr.model.MessageResponse;
import com.hr_management.hr.model.ProfileUpdateDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.service.FileStorageService;
import com.hr_management.hr.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile Management", description = "Profile management APIs")
public class ProfileController {

    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;

    public ProfileController(
            UserService userService,
            EmployeeRepository employeeRepository,
            FileStorageService fileStorageService) {
        this.userService = userService;
        this.employeeRepository = employeeRepository;
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

    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            @RequestPart(value = "file", required = true) MultipartFile file,
            HttpServletRequest request) {
        try {
            System.out.println("Received file upload request");
            System.out.println("Request Content-Type: " + request.getContentType());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Request Headers:");
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println(headerName + ": " + request.getHeader(headerName));
            }
            
            System.out.println("File details:");
            System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("File size: " + (file != null ? file.getSize() : "null"));
            System.out.println("File content type: " + (file != null ? file.getContentType() : "null"));
            System.out.println("Is empty: " + (file != null ? file.isEmpty() : "null"));
            
            User user = (User) authentication.getPrincipal();
            if (user == null) {
                System.out.println("User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("User not authenticated"));
            }

            if (file == null || file.isEmpty()) {
                System.out.println("File is null or empty");
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File is required and cannot be empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream"; // Default content type if not provided
            }
            
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Only image files are allowed. Received content type: " + contentType));
            }

            // Store the file and get the path
            String filePath = fileStorageService.storeFile(file, "profile_" + user.getId());
            user.setProfilePicture(filePath);
            userService.updateProfile(user.getId(), new ProfileUpdateDto());

            return ResponseEntity.ok(new MessageResponse("Profile picture updated successfully"));
        } catch (IOException e) {
            System.out.println("IOException during file upload: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to store profile picture: " + e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("RuntimeException during file upload: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }
} 