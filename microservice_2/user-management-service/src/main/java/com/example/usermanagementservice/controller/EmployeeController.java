package com.example.usermanagementservice.controller;

import com.example.usermanagementservice.client.AccessControlClient;
import com.example.usermanagementservice.model.Employee;
import com.example.usermanagementservice.model.EmployeeStatistics;
import com.example.usermanagementservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AccessControlClient accessControlClient;

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

     @GetMapping("/employee-statistics")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Object> accessLogs = accessControlClient.getAccessLogsByTimeRange(startDate, endDate);
        
        List<Employee> employees = employeeRepository.findAll();
        
        List<Map<String, Object>> statistics = employees.stream()
            .map(employee -> {
                List<Map<String, Object>> employeeLogs = accessLogs.stream()
                    .map(log -> (Map<String, Object>) log)
                    .filter(log -> employee.getId().equals(log.get("employeeId")))
                    .collect(Collectors.toList());
                
                long authorizedAccess = employeeLogs.stream()
                    .filter(log -> (Boolean) log.get("authorized"))
                    .count();
                
                long unauthorizedAccess = employeeLogs.size() - authorizedAccess;
                Map<String, Object> stats = new HashMap<>();
                stats.put("employeeId", employee.getId());
                stats.put("fullName", employee.getFullName());
                stats.put("photoUrl", employee.getPhoto());
                stats.put("totalAccesses", employeeLogs.size());
                stats.put("authorizedAccess", authorizedAccess);
                stats.put("unauthorizedAccess", unauthorizedAccess);
                return stats;
                
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(statistics);
    }
}