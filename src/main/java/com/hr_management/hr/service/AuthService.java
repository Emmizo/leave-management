package com.hr_management.hr.service;

import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.RegisterRequestDto;

public interface AuthService {
    AuthResponse register(RegisterRequestDto request);
    void resetPassword(String email, String newPassword);
} 