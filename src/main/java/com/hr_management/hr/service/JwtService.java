package com.hr_management.hr.service;

import com.hr_management.hr.entity.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token, User user);
} 