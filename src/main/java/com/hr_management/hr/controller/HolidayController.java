package com.hr_management.hr.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hr_management.hr.model.HolidayDto;
import com.hr_management.hr.service.HolidayService;

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
@RequestMapping("/api/holidays")
@Tag(name = "Holiday Management", description = "Holiday management APIs")
public class HolidayController {

    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);
    
    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @PostMapping("/test-request")
    @Operation(summary = "Test request body", description = "Tests if the request body is being processed correctly.")
    public ResponseEntity<Object> testRequest(@RequestBody Object requestBody) {
        logger.info("Received request body: {}", requestBody);
        return ResponseEntity.ok(requestBody);
    }

    @GetMapping("/test-auth")
    @Operation(summary = "Test authentication", description = "Tests if the current user is authenticated and has the required roles.")
    public ResponseEntity<String> testAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        
        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        boolean hasAdminRole = roles.contains("ROLE_ADMIN");
        boolean hasHrManagerRole = roles.contains("ROLE_HR_MANAGER");
        
        String message = String.format("Authenticated as: %s, Roles: %s, Has ADMIN: %s, Has HR_MANAGER: %s",
                username, roles, hasAdminRole, hasHrManagerRole);
        
        return ResponseEntity.ok(message);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    @Operation(summary = "Create a new holiday", 
               description = "Creates a new holiday in the system. Only accessible by ADMIN and HR_MANAGER roles.",
               security = @SecurityRequirement(name = "bearerAuth"),
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "Holiday details for creation",
                   required = true,
                   content = @Content(
                       mediaType = "application/json",
                       schema = @Schema(implementation = HolidayDto.class)
                   )
               ))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Holiday created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions")
    })
    public ResponseEntity<HolidayDto> createHoliday(
            @Valid @RequestBody HolidayDto holidayDto,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            logger.info("Creating new holiday: {}", holidayDto);
            
            if (authentication == null) {
                logger.error("Authentication is null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            String username = authentication.getName();
            logger.info("User creating holiday: {}", username);
            
            // Ensure ID is null for creation
            holidayDto.setId(null);
            
            HolidayDto createdHoliday = holidayService.createHoliday(holidayDto, username);
            logger.info("Holiday created successfully with ID: {}", createdHoliday.getId());
            
            return new ResponseEntity<>(createdHoliday, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating holiday", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    @Operation(summary = "Update a holiday", 
               description = "Updates an existing holiday in the system. Only accessible by ADMIN and HR_MANAGER roles.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holiday updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Holiday not found")
    })
    public ResponseEntity<HolidayDto> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayDto holidayDto) {
        try {
            logger.info("Updating holiday with ID: {}", id);
            HolidayDto updatedHoliday = holidayService.updateHoliday(id, holidayDto);
            return ResponseEntity.ok(updatedHoliday);
        } catch (Exception e) {
            logger.error("Error updating holiday", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR_MANAGER')")
    @Operation(summary = "Delete a holiday", 
               description = "Deletes a holiday from the system. Only accessible by ADMIN and HR_MANAGER roles.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Holiday deleted successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Holiday not found")
    })
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        try {
            logger.info("Deleting holiday with ID: {}", id);
            holidayService.deleteHoliday(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting holiday", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a holiday by ID", 
               description = "Retrieves a holiday by its ID. Accessible by all authenticated users.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holiday retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "Holiday not found")
    })
    public ResponseEntity<HolidayDto> getHolidayById(@PathVariable Long id) {
        try {
            logger.info("Getting holiday with ID: {}", id);
            HolidayDto holidayDto = holidayService.getHolidayById(id);
            return ResponseEntity.ok(holidayDto);
        } catch (Exception e) {
            logger.error("Error getting holiday", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all holidays", 
               description = "Retrieves all holidays in the system. Accessible by all authenticated users.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<HolidayDto>> getAllHolidays() {
        try {
            logger.info("Getting all holidays");
            List<HolidayDto> holidays = holidayService.getAllHolidays();
            return ResponseEntity.ok(holidays);
        } catch (Exception e) {
            logger.error("Error getting all holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming holidays", 
               description = "Retrieves all upcoming holidays. Accessible by all authenticated users.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming holidays retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<HolidayDto>> getUpcomingHolidays() {
        try {
            logger.info("Getting upcoming holidays");
            List<HolidayDto> upcomingHolidays = holidayService.getUpcomingHolidays();
            return ResponseEntity.ok(upcomingHolidays);
        } catch (Exception e) {
            logger.error("Error getting upcoming holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get holidays by date range", 
               description = "Retrieves holidays within a specified date range. Accessible by all authenticated users.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Holidays retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<HolidayDto>> getHolidaysByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Getting holidays by date range: {} to {}", startDate, endDate);
            List<HolidayDto> holidays = holidayService.getHolidaysByDateRange(startDate, endDate);
            return ResponseEntity.ok(holidays);
        } catch (Exception e) {
            logger.error("Error getting holidays by date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recurring")
    @Operation(summary = "Get recurring holidays", 
               description = "Retrieves all recurring holidays. Accessible by all authenticated users.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recurring holidays retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<HolidayDto>> getRecurringHolidays() {
        try {
            logger.info("Getting recurring holidays");
            List<HolidayDto> recurringHolidays = holidayService.getRecurringHolidays();
            return ResponseEntity.ok(recurringHolidays);
        } catch (Exception e) {
            logger.error("Error getting recurring holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 