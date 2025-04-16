package com.hr_management.hr.service;

import java.util.List;
import java.util.Optional;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.model.EmployeeDto;

public interface EmployeeService {
    EmployeeDto save(EmployeeDto employeeDto);
    Optional<EmployeeDto> findByUser(User user);
    Optional<Employee> findById(Long id);
    void deleteById(Long id);
    List<EmployeeDto> findAllEmployees();
} 