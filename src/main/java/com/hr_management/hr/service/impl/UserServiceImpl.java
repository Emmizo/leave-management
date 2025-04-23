package com.hr_management.hr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Role;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.exception.ResourceNotFoundException;
import com.hr_management.hr.model.ProfileUpdateDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.UserService;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User createUser(String username, String password, String email) {
        if (existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        logger.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "user", userId));

        // Update User entity fields
        if (profileUpdateDto.getUsername() != null && !profileUpdateDto.getUsername().equals(user.getUsername())) {
            if (existsByUsername(profileUpdateDto.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(profileUpdateDto.getUsername());
        }

        if (profileUpdateDto.getEmail() != null && !profileUpdateDto.getEmail().equals(user.getEmail())) {
            if (existsByEmail(profileUpdateDto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(profileUpdateDto.getEmail());
            employee.setEmail(profileUpdateDto.getEmail());
        }

        // Update Employee entity fields
        if (profileUpdateDto.getFirstName() != null) {
            employee.setFirstName(profileUpdateDto.getFirstName());
        }
        if (profileUpdateDto.getLastName() != null) {
            employee.setLastName(profileUpdateDto.getLastName());
        }
        if (profileUpdateDto.getDepartment() != null) {
            employee.setDepartment(profileUpdateDto.getDepartment());
        }
        if (profileUpdateDto.getPosition() != null) {
            employee.setPosition(profileUpdateDto.getPosition());
        }
        if (profileUpdateDto.getPhone() != null) {
            employee.setPhone(profileUpdateDto.getPhone());
        }

        // Save both entities
        employeeRepository.save(employee);
        return userRepository.save(user);
    }
}