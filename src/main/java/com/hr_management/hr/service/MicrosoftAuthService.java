package com.hr_management.hr.service;

import java.util.Map;

public interface MicrosoftAuthService {
    String getAuthorizationUrl(String state);
    Map<String, Object> exchangeCodeForTokens(String code);
    Map<String, Object> getUserInfo(String accessToken);
    com.hr_management.hr.entity.User createOrUpdateUser(Map<String, Object> userInfo);

    /**
     * Updates the profile picture URL for an existing Microsoft user
     * @param user The user to update
     * @return The updated user with new profile picture URL
     */
    com.hr_management.hr.entity.User updateProfilePicture(com.hr_management.hr.entity.User user);
} 