package com.hr_management.hr.model;

import java.time.LocalDateTime;
import java.util.List;

import com.hr_management.hr.enums.Gender;

public class EmployeeDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String position;
    private String phone;
    private Integer annualLeaveBalance;
    private String microsoftId;
    private UserDto user;
    private List<LeaveDto> leaves;
    private LocalDateTime createdAt;
    private Gender gender;

    // Default constructor
    public EmployeeDto() {
    }

    // All-args constructor
    public EmployeeDto(Long id, String email, String firstName, String lastName, 
                      String department, String position, String phone, Integer annualLeaveBalance, 
                      String microsoftId, UserDto user, List<LeaveDto> leaves, LocalDateTime createdAt,
                      Gender gender) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.annualLeaveBalance = annualLeaveBalance;
        this.microsoftId = microsoftId;
        this.user = user;
        this.leaves = leaves;
        this.createdAt = createdAt;
        this.gender = gender;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String department;
        private String position;
        private String phone;
        private Integer annualLeaveBalance;
        private String microsoftId;
        private UserDto user;
        private List<LeaveDto> leaves;
        private LocalDateTime createdAt;
        private Gender gender;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder position(String position) {
            this.position = position;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder annualLeaveBalance(Integer annualLeaveBalance) {
            this.annualLeaveBalance = annualLeaveBalance;
            return this;
        }

        public Builder microsoftId(String microsoftId) {
            this.microsoftId = microsoftId;
            return this;
        }

        public Builder user(UserDto user) {
            this.user = user;
            return this;
        }

        public Builder leaves(List<LeaveDto> leaves) {
            this.leaves = leaves;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public EmployeeDto build() {
            return new EmployeeDto(id, email, firstName, lastName, department, 
                                 position, phone, annualLeaveBalance, microsoftId, user, 
                                 leaves, createdAt, gender);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAnnualLeaveBalance() {
        return annualLeaveBalance;
    }

    public void setAnnualLeaveBalance(Integer annualLeaveBalance) {
        this.annualLeaveBalance = annualLeaveBalance;
    }

    public String getMicrosoftId() {
        return microsoftId;
    }

    public void setMicrosoftId(String microsoftId) {
        this.microsoftId = microsoftId;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public List<LeaveDto> getLeaves() {
        return leaves;
    }

    public void setLeaves(List<LeaveDto> leaves) {
        this.leaves = leaves;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
        EmployeeDto that = (EmployeeDto) o;
        return (id != null ? id.equals(that.id) : that.id == null) &&
                (email != null ? email.equals(that.email) : that.email == null) &&
                (firstName != null ? firstName.equals(that.firstName) : that.firstName == null) &&
                (lastName != null ? lastName.equals(that.lastName) : that.lastName == null) &&
                (department != null ? department.equals(that.department) : that.department == null) &&
                (position != null ? position.equals(that.position) : that.position == null) &&
                (phone != null ? phone.equals(that.phone) : that.phone == null) &&
                (annualLeaveBalance != null ? annualLeaveBalance.equals(that.annualLeaveBalance) : that.annualLeaveBalance == null) &&
                (microsoftId != null ? microsoftId.equals(that.microsoftId) : that.microsoftId == null) &&
                (user != null ? user.equals(that.user) : that.user == null) &&
                (leaves != null ? leaves.equals(that.leaves) : that.leaves == null) &&
                (createdAt != null ? createdAt.equals(that.createdAt) : that.createdAt == null) &&
                (gender != null ? gender.equals(that.gender) : that.gender == null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (annualLeaveBalance != null ? annualLeaveBalance.hashCode() : 0);
        result = 31 * result + (microsoftId != null ? microsoftId.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (leaves != null ? leaves.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", phone='" + phone + '\'' +
                ", annualLeaveBalance=" + annualLeaveBalance +
                ", microsoftId='" + microsoftId + '\'' +
                ", user=" + user +
                ", leaves=" + leaves +
                ", createdAt=" + createdAt +
                ", gender=" + gender +
                '}';
    }
} 