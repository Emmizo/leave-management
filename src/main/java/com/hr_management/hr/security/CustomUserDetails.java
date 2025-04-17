package com.hr_management.hr.security;

import com.hr_management.hr.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final User user;
    private Map<String, Object> attributes; // Store OAuth2 attributes if needed

    public CustomUserDetails(User user) {
        this.user = user;
    }
    
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // --- UserDetails methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // Or user.getEmail() depending on your preference
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public Long getId(){
        return user.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
    
    // --- OAuth2User methods ---
    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // Return attributes received from OAuth2 provider
    }

    @Override
    public String getName() {
        // Return a unique identifier from attributes, e.g., email or Microsoft OID
        // Or return the local username/ID
        return String.valueOf(user.getId()); 
    }
    
    // --- Helper method to get the local User entity ---
    public User getUser() {
        return user;
    }
} 