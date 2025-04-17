package com.hr_management.hr.security;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder; // Used for consistency if creating users

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        if (email == null) {
             // Try 'upn' (User Principal Name) as a fallback for email
            email = (String) attributes.get("upn");
        }
        String name = (String) attributes.get("name");
        String microsoftId = (String) attributes.get("oid"); // Object ID from Azure AD

        if (email == null) {
            log.error("Email not found from OAuth2 provider");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        log.info("Processing OAuth2 user: Email={}, Name={}, MicrosoftID={}", email, name, microsoftId);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            log.info("Found existing user: {}", email);
            // Optionally update user details (name, Microsoft ID) if they have changed
            updateExistingUser(user, name, microsoftId);
        } else {
            log.info("Creating new user: {}", email);
            user = createNewUser(email, name, microsoftId);
        }

        // Important: Return a custom principal that wraps your User entity
        // This makes it easier to retrieve your local User details later
        return new CustomUserDetails(user, oauth2User.getAttributes()); 
    }

    private void updateExistingUser(User user, String name, String microsoftId) {
        // Example: Update name if provided and different
        // Update Microsoft ID if not already set
        if (microsoftId != null && user.getMicrosoftId() == null) {
             log.debug("Updating Microsoft ID for user {}", user.getEmail());
             user.setMicrosoftId(microsoftId);
             userRepository.save(user);
        }
        // Potentially update the associated Employee's name if desired
    }

    private User createNewUser(String email, String name, String microsoftId) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(email); // Use email as username for simplicity
        newUser.setMicrosoftId(microsoftId);
        // Create a dummy password or handle password management differently for OAuth users
        // For this example, we'll set a random, unusable password
        newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        newUser.setRole(Role.USER); // Assign a default role
        newUser.setEnabled(true); // Assuming OAuth users are enabled by default

        User savedUser = userRepository.save(newUser);

        // Optionally, create a corresponding Employee record
        createAssociatedEmployee(savedUser, name);

        return savedUser;
    }

    private void createAssociatedEmployee(User user, String name) {
        if (!employeeRepository.findByUser(user).isPresent()) {
            log.info("Creating associated Employee record for user {}", user.getEmail());
            Employee newEmployee = new Employee();
            newEmployee.setUser(user);
            newEmployee.setEmail(user.getEmail());
            // Attempt to split name into first and last
            String firstName = name; 
            String lastName = "";
            if (name != null && name.contains(" ")) {
                int lastSpace = name.lastIndexOf(" ");
                firstName = name.substring(0, lastSpace);
                lastName = name.substring(lastSpace + 1);
            } else if (name == null){
                 firstName = "User"; // Default if name is null
            }

            newEmployee.setFirstName(firstName);
            newEmployee.setLastName(lastName);
            // Set default values for other required Employee fields if necessary
            newEmployee.setDepartment("Default"); 
            newEmployee.setPosition("Default");
            newEmployee.setAnnualLeaveBalance(20); // Default leave balance
            
            employeeRepository.save(newEmployee);
        } else {
            log.debug("Employee record already exists for user {}", user.getEmail());
        }
    }
} 