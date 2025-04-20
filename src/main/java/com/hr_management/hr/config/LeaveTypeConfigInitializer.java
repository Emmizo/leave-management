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
                    config.setAnnualLimit(20);
                    config.setRequiresDocument(false);
                    config.setDescription("Paid Time Off - Standard annual leave");
                }
                case SICK -> {
                    config.setAnnualLimit(10);
                    config.setRequiresDocument(true);
                    config.setDescription("Sick Leave - Requires medical certificate");
                }
                case BEREAVEMENT -> {
                    config.setAnnualLimit(5);
                    config.setRequiresDocument(true);
                    config.setDescription("Bereavement Leave - Requires death certificate");
                }
                case MATERNITY -> {
                    config.setAnnualLimit(90);
                    config.setRequiresDocument(true);
                    config.setDescription("Maternity Leave - Requires medical certificate");
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