package com.hr_management.hr.model;

import com.hr_management.hr.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Department cannot be blank")
    private String department;

    @NotBlank(message = "Position is required")
    private String position;

    private String phone;

    @NotBlank(message = "Role is required")
    private String role;

    private Gender gender;

    // Default constructor
    public RegisterRequestDto() {
    }

    // All-args constructor
    public RegisterRequestDto(String username, String password, String email, 
                             String firstName, String lastName, String department, 
                             String position, String role, Gender gender) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.position = position;
        this.role = role;
        this.gender = gender;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterRequestDto that = (RegisterRequestDto) o;
        return (username != null ? username.equals(that.username) : that.username == null) &&
                (password != null ? password.equals(that.password) : that.password == null) &&
                (email != null ? email.equals(that.email) : that.email == null) &&
                (firstName != null ? firstName.equals(that.firstName) : that.firstName == null) &&
                (lastName != null ? lastName.equals(that.lastName) : that.lastName == null) &&
                (department != null ? department.equals(that.department) : that.department == null) &&
                (position != null ? position.equals(that.position) : that.position == null) &&
                (role != null ? role.equals(that.role) : that.role == null) &&
                (gender != null ? gender.equals(that.gender) : that.gender == null);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegisterRequestDto{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", role='" + role + '\'' +
                ", gender=" + gender +
                '}';
    }
} 