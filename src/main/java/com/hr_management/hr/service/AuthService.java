package com.hr_management.hr.service;

import com.hr_management.hr.model.AuthResponse;
import com.hr_management.hr.model.ForgotPasswordRequestDto;
import com.hr_management.hr.model.RegisterRequestDto;
import com.hr_management.hr.model.ResetPasswordRequestDto;

public interface AuthService {
    AuthResponse register(RegisterRequestDto request);
    void resetPassword(String email, String newPassword);
    void forgotPassword(ForgotPasswordRequestDto request);
    void resetPasswordWithToken(ResetPasswordRequestDto request);
} 