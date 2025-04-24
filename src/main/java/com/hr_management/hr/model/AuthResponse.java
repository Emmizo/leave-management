package com.hr_management.hr.model;

public class AuthResponse {
    private String token;
    private EmployeeDto user;
    private String profilePictureUrl;

    // Default constructor
    public AuthResponse() {
    }

    // All-args constructor
    public AuthResponse(String token, EmployeeDto user, String profilePictureUrl) {
        this.token = token;
        this.user = user;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private EmployeeDto user;
        private String profilePictureUrl;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder user(EmployeeDto user) {
            this.user = user;
            return this;
        }

        public Builder profilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
            return this;
        }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.token = this.token;
            response.user = this.user;
            response.profilePictureUrl = this.profilePictureUrl;
            return response;
        }
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public EmployeeDto getUser() {
        return user;
    }

    public void setUser(EmployeeDto user) {
        this.user = user;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return (token != null ? token.equals(that.token) : that.token == null) &&
                (user != null ? user.equals(that.user) : that.user == null) &&
                (profilePictureUrl != null ? profilePictureUrl.equals(that.profilePictureUrl) : that.profilePictureUrl == null);
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (profilePictureUrl != null ? profilePictureUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", user=" + user +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                '}';
    }
} 