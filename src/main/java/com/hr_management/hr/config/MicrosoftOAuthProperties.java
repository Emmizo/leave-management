package com.hr_management.hr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "microsoft")
public class MicrosoftOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String tenantId = "202c75b4-8389-4dce-b7a9-9d2c4f7b1bad"; // Default tenant ID from application.properties
    private String redirectUri = "http://localhost:3000/microsoft-callback";
    private String scope = "openid profile email User.Read";
    private String authorizationEndpoint = "https://login.microsoftonline.com/202c75b4-8389-4dce-b7a9-9d2c4f7b1bad/oauth2/v2.0/authorize";
    private String tokenEndpoint = "https://login.microsoftonline.com/202c75b4-8389-4dce-b7a9-9d2c4f7b1bad/oauth2/v2.0/token";
    private String userInfoEndpoint = "https://graph.microsoft.com/v1.0/me";

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }
} 