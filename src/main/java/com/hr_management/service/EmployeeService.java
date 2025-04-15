package com.hr_management.service;

import com.hr_management.entity.Employee;
import com.hr_management.entity.User;
import java.util.Optional;

public interface EmployeeService {
    Employee save(Employee employee);
    Optional<Employee> findByUser(User user);
} 