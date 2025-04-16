package com.hr_management.hr.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hr_management.hr.entity.Employee;
import com.hr_management.hr.entity.Leave;
import com.hr_management.hr.entity.User;
import com.hr_management.hr.model.EmployeeDto;
import com.hr_management.hr.model.LeaveDto;
import com.hr_management.hr.model.UserDto;
import com.hr_management.hr.repository.EmployeeRepository;
import com.hr_management.hr.repository.LeaveRepository;
import com.hr_management.hr.repository.UserRepository;
import com.hr_management.hr.service.EmployeeService;
import com.hr_management.hr.service.LeaveService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveService leaveService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository, LeaveService leaveService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.leaveService = leaveService;
    }

    @Override
    public EmployeeDto save(EmployeeDto employeeDto) {
        Employee employee = new Employee();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setPosition(employeeDto.getPosition());
        employee.setEmail(employeeDto.getEmail());
        
        // Set default annual leave balance if not provided
        employee.setAnnualLeaveBalance(
            employeeDto.getAnnualLeaveBalance() != null ? 
            employeeDto.getAnnualLeaveBalance() : 20
        );
        
        // Set default Microsoft ID if not provided
        employee.setMicrosoftId(
            employeeDto.getMicrosoftId() != null ? 
            employeeDto.getMicrosoftId() : 
            "MS_" + employeeDto.getEmail().replaceAll("[^a-zA-Z0-9]", "_")
        );
        
        // Set user relationship
        if (employeeDto.getUser() != null && employeeDto.getUser().getId() != null) {
            User user = userRepository.findById(employeeDto.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            employee.setUser(user);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDto(savedEmployee);
    }

    @Override
    public Optional<EmployeeDto> findByUser(User user) {
        return employeeRepository.findByUser(user)
                .map(this::convertToDto);
    }
    
    @Override
    public List<EmployeeDto> findAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private EmployeeDto convertToDto(Employee employee) {
        UserDto userDto = null;
        if (employee.getUser() != null) {
            userDto = UserDto.builder()
                    .id(employee.getUser().getId())
                    .username(employee.getUser().getUsername())
                    .email(employee.getUser().getEmail())
                    .role(employee.getUser().getRole().name())
                    .build();
        }

        // Fetch and convert leaves
        List<LeaveDto> leaveDtos = leaveService.getEmployeeLeaves(employee.getId());

        return EmployeeDto.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .annualLeaveBalance(employee.getAnnualLeaveBalance())
                .microsoftId(employee.getMicrosoftId())
                .user(userDto)
                .leaves(leaveDtos)
                .build();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }
} 