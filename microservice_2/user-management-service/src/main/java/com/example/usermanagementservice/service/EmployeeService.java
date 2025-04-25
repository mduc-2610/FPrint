package com.example.usermanagementservice.service;

import com.example.usermanagementservice.client.AccessControlClient;
import com.example.usermanagementservice.model.Employee;
import com.example.usermanagementservice.model.EmployeeStatistics;
import com.example.usermanagementservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AccessControlClient accessControlClient;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save((Employee) userService.createUser(employee));
    }

    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save((Employee) userService.updateUser(employee));
    }

    public void deleteEmployee(String id) {
        employeeRepository.deleteById(id);
    }

    public EmployeeStatistics getEmployeeStatistics(String employeeId) {
        Optional<Employee> employee = getEmployeeById(employeeId);
        if (!employee.isPresent()) {
            throw new RuntimeException("Employee not found");
        }

        List<Object> accessLogs = accessControlClient.getAccessLogsByEmployeeId(employeeId);

        // This is simplified and would need proper implementation using actual data
        long totalAccesses = accessLogs.size();
        LocalDateTime firstAccessTime = LocalDateTime.now().minusDays(30);
        LocalDateTime lastAccessTime = LocalDateTime.now();

        return new EmployeeStatistics(
                employeeId,
                employee.get().getFullName(),
                totalAccesses,
                firstAccessTime,
                lastAccessTime
        );
    }
}
