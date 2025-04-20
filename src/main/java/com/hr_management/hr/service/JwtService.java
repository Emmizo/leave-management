package com.hr_management.hr.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    /**
     * Extracts the username from a JWT token
     * @param token JWT token
     * @return username
     */
    String extractUsername(String token);
    
    /**
     * Generates a JWT token for the given user details
     * @param userDetails user details
     * @return JWT token
     */
    String generateToken(UserDetails userDetails);
    
    /**
     * Validates if a token is valid for the given user details
     * @param token JWT token
     * @param userDetails user details
     * @return true if valid, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
} 