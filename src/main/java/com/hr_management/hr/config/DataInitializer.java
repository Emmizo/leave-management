package com.hr_management.hr.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.enums.Gender;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Running data initializer...");
        // Ensure default Admin/HR users and employees exist
        createDefaultUserAndEmployee("admin", "admin@example.com", "admin123", Role.ADMIN, "Admin", "User", "Administration", "System Administrator", Gender.MALE);
        createDefaultUserAndEmployee("hrmanager", "hr@example.com", "hrmanager123", Role.HR_MANAGER, "HR", "Manager", "Human Resources", "HR Manager", Gender.FEMALE);

        // Optional: Check and create missing Employee records for *any* existing User
        // Be cautious with this in production if users can exist without employees intentionally
        /* 
        log.info("Checking for users missing employee records...");
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!employeeRepository.existsByUser(user)) {
                log.warn("User {} is missing an employee record. Creating one with default values.", user.getUsername());
                // Use default values or fetch required info if possible
                createEmployeeForUser(user, "DefaultFirst", "DefaultLast", "DefaultDept", "DefaultPos", Gender.OTHER); 
            }
        }
        */
        log.info("Data initializer finished.");
    }

    private void createDefaultUserAndEmployee(String username, String email, String password, Role role, 
                                              String firstName, String lastName, String department, String position,
                                              Gender gender) {
        Optional<User> existingUserOpt = userRepository.findByUsername(username);

        if (existingUserOpt.isEmpty()) {
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password)) 
                    .role(role)
                    .build();
            User savedUser = userRepository.save(newUser);
            log.info("Created default user: {}", username);
            createEmployeeForUser(savedUser, firstName, lastName, department, position, gender);
        } else {
            User existingUser = existingUserOpt.get();
            if (!employeeRepository.existsByUser(existingUser)) {
                log.warn("User {} exists, but is missing an employee record. Creating one.", username);
                createEmployeeForUser(existingUser, firstName, lastName, department, position, gender);
            }
        }
    }

    private void createEmployeeForUser(User user, String firstName, String lastName, String department, String position, Gender gender) {
        if (user == null) {
             log.error("Attempted to create employee for a null user.");
             return;
        }
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmail(user.getEmail());
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setAnnualLeaveBalance(20); // Default value
        employee.setGender(gender);
        // Generate a default MS ID if needed, ensure it's not null if DB requires it
        employee.setMicrosoftId(user.getProviderId() != null ? user.getProviderId() : "MS_" + user.getEmail().replaceAll("[^a-zA-Z0-9]", "_")); 
        try {
             employeeRepository.save(employee);
             log.info("Created/updated employee record for user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Failed to save employee record for user {}: {}", user.getUsername(), e.getMessage());
            // Handle potential constraint violations, e.g., if email must be unique in Employee table too
        }
    }
}