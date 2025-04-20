package com.hr_management.hr.model;

public class LoginRequestDto {
    private String username;
    private String password;

    // Default constructor
    public LoginRequestDto() {
    }

    // All-args constructor
    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginRequestDto that = (LoginRequestDto) o;
        return (username != null ? username.equals(that.username) : that.username == null) &&
                (password != null ? password.equals(that.password) : that.password == null);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LoginRequestDto{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
} 