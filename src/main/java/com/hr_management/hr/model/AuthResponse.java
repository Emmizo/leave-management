package com.hr_management.hr.model;

public class AuthResponse {
    private String token;
    private EmployeeDto user;

    // Default constructor
    public AuthResponse() {
    }

    // All-args constructor
    public AuthResponse(String token, EmployeeDto user) {
        this.token = token;
        this.user = user;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private EmployeeDto user;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder user(EmployeeDto user) {
            this.user = user;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, user);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthResponse that = (AuthResponse) o;
        return (token != null ? token.equals(that.token) : that.token == null) &&
                (user != null ? user.equals(that.user) : that.user == null);
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", user=" + user +
                '}';
    }
} 