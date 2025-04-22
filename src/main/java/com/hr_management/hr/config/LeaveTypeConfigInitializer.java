package com.hr_management.hr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.repository.LeaveTypeConfigRepository;

@Component
public class LeaveTypeConfigInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LeaveTypeConfigInitializer.class);
    private final LeaveTypeConfigRepository leaveTypeConfigRepository;

    public LeaveTypeConfigInitializer(LeaveTypeConfigRepository leaveTypeConfigRepository) {
        this.leaveTypeConfigRepository = leaveTypeConfigRepository;
    }

    @Override
    public void run(String... args) {
        initLeaveTypeConfig();
    }

    private void initLeaveTypeConfig() {
        // Check if configurations already exist
        if (leaveTypeConfigRepository.count() > 0) {
            log.info("Leave type configurations already initialized");
            return;
        }

        log.info("Initializing leave type configurations");

        // Create configurations for each leave type
        for (LeaveType leaveType : LeaveType.values()) {
            LeaveTypeConfig config = new LeaveTypeConfig();
            config.setLeaveType(leaveType);
            config.setIsActive(true);
            
            // Set default values based on leave type
            switch (leaveType) {
                case PTO -> {
                    config.setAnnualLimit(20); // 20 days per year = 1.66 days per month
                    config.setRequiresDocument(false);
                    config.setDescription("Paid Time Off - Standard annual leave (1.66 days per month)");
                }
                case SICK -> {
                    config.setAnnualLimit(10); // 10 days per year = 0.83 days per month
                    config.setRequiresDocument(true);
                    config.setDescription("Sick Leave - Requires medical certificate (0.83 days per month)");
                }
                case COMPASSIONATE -> {
                    config.setAnnualLimit(5); // 5 days per year = 0.42 days per month
                    config.setRequiresDocument(true);
                    config.setDescription("Compassionate Leave - Requires death certificate (0.42 days per month)");
                }
                case MATERNITY -> {
                    config.setAnnualLimit(90); // 90 days per year = 7.5 days per month
                    config.setRequiresDocument(true);
                    config.setDescription("Maternity Leave - Requires medical certificate (7.5 days per month)");
                }
                case PATERNITY -> {
                    config.setAnnualLimit(14); // 14 days per year = 1.17 days per month
                    config.setRequiresDocument(true);
                    config.setDescription("Paternity Leave - For new fathers (1.17 days per month)");
                }
                case UNPAID -> {
                    config.setAnnualLimit(365); // Unlimited
                    config.setRequiresDocument(false);
                    config.setDescription("Unpaid Leave - No monthly limit");
                }
                case OTHER -> {
                    config.setAnnualLimit(0);
                    config.setRequiresDocument(false);
                    config.setDescription("Other Leave - Requires approval");
                }
                default -> {
                    config.setAnnualLimit(0);
                    config.setRequiresDocument(false);
                    config.setDescription("Default configuration");
                }
            }
            
            leaveTypeConfigRepository.save(config);
            log.info("Created configuration for leave type: {}", leaveType);
        }
        
        log.info("Leave type configurations initialized successfully");
    }
} 