package com.hr_management.hr.model;

public class AuthResponse {
    private String token;
    private EmployeeDto user;
    private String profilePicture;

    // Default constructor
    public AuthResponse() {
    }

    // All-args constructor
    public AuthResponse(String token, EmployeeDto user, String profilePicture) {
        this.token = token;
        this.user = user;
        this.profilePicture = profilePicture;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return (token != null ? token.equals(that.token) : that.token == null) &&
                (user != null ? user.equals(that.user) : that.user == null) &&
                (profilePicture != null ? profilePicture.equals(that.profilePicture) : that.profilePicture == null);
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (profilePicture != null ? profilePicture.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", user=" + user +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private EmployeeDto user;
        private String profilePicture;

        public AuthResponseBuilder token(String token) {
            this.token = token;
            return this;
        }

        public AuthResponseBuilder user(EmployeeDto user) {
            this.user = user;
            return this;
        }

        public AuthResponseBuilder profilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
            return this;
        }

        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.setToken(this.token);
            response.setUser(this.user);
            response.setProfilePicture(this.profilePicture);
            return response;
        }
    }
} 