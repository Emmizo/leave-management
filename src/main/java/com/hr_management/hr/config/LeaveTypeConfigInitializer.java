package com.hr_management.hr.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import jakarta.annotation.PostConstruct;

import com.hr_management.hr.entity.LeaveTypeConfig;
import com.hr_management.hr.enums.LeaveType;
import com.hr_management.hr.repository.LeaveTypeConfigRepository;

@Configuration
@DependsOn("leaveTypeConfigRepository")
public class LeaveTypeConfigInitializer {

    @Bean
    @PostConstruct
    CommandLineRunner initLeaveTypeConfig(LeaveTypeConfigRepository leaveTypeConfigRepository) {
        return args -> {
            // Initialize PTO (Personal Time Off)
            createIfNotExists(leaveTypeConfigRepository, LeaveType.PTO, 20,
                "Standard annual leave for personal time off.",
                false);

            // Initialize Sick Leave
            createIfNotExists(leaveTypeConfigRepository, LeaveType.SICK, 10,
                "Leave for medical reasons. Requires medical proof.",
                true);

            // Initialize Maternity Leave
            createIfNotExists(leaveTypeConfigRepository, LeaveType.MATERNITY, 90,
                "Extended leave for expecting mothers. Requires medical documentation.",
                true);

            // Initialize Paternity Leave
            createIfNotExists(leaveTypeConfigRepository, LeaveType.PATERNITY, 14,
                "Leave for new fathers. May require birth certificate.",
                true);

            // Initialize Bereavement Leave
            createIfNotExists(leaveTypeConfigRepository, LeaveType.BEREAVEMENT, 5,
                "Leave for family bereavement. May require death certificate.",
                true);

            // Initialize Unpaid Leave
            createIfNotExists(leaveTypeConfigRepository, LeaveType.UNPAID, 30,
                "Unpaid leave for special circumstances. Requires manager approval.",
                false);
        };
    }

    private void createIfNotExists(LeaveTypeConfigRepository repository, LeaveType type, 
            int annualLimit, String description, boolean requiresDocument) {
        if (!repository.findByLeaveType(type).isPresent()) {
            LeaveTypeConfig config = new LeaveTypeConfig();
            config.setLeaveType(type);
            config.setAnnualLimit(annualLimit);
            config.setDescription(description);
            config.setRequiresDocument(requiresDocument);
            config.setIsActive(true);
            repository.save(config);
        }
    }
} 