package com.hr_management.hr.service;

import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.ProfileUpdateDto;

public interface UserService {
    User createUser(String username, String password, String email);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto);
}