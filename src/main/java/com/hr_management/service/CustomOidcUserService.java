package com.hr_management.service;

import com.hr_management.entity.Employee;
import com.hr_management.entity.User;
import com.hr_management.repository.EmployeeRepository;
import com.hr_management.repository.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final Set<String> allowedDomains = Set.of("@gmail.com", "@ist.com");

    public CustomOidcUserService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        
        // Validate email domain
        boolean isValidDomain = allowedDomains.stream()
                .anyMatch(domain -> email.toLowerCase().endsWith(domain));
        
        if (!isValidDomain) {
            throw new OAuth2AuthenticationException("Email domain not allowed. Only @gmail.com and @ist.com are permitted.");
        }

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(oidcUser.getFullName());
            newUser.setProvider("microsoft");
            newUser.setProviderId(oidcUser.getSubject());
            userRepository.save(newUser);

            // Create new employee if not exists
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);
            if (existingEmployee.isEmpty()) {
                Employee newEmployee = new Employee();
                newEmployee.setEmail(email);
                String[] nameParts = oidcUser.getFullName().split(" ", 2);
                newEmployee.setFirstName(nameParts[0]);
                newEmployee.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                newEmployee.setDepartment("Default");
                newEmployee.setPosition("Employee");
                employeeRepository.save(newEmployee);
            }
        }

        return oidcUser;
    }
} 