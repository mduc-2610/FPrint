package com.example.fingerprint_backend.controller;

import com.example.fingerprint_backend.model.AccessLog;
import com.example.fingerprint_backend.model.Employee;
import com.example.fingerprint_backend.repository.AccessLogRepository;
import com.example.fingerprint_backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/access-log")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogRepository accessLogRepository;
    private final EmployeeRepository employeeRepository;


    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<AccessLog>> getAccessLogsByEmployee(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String accessType,
            @RequestParam(required = false) String areaId
    ) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<AccessLog> accessLogs = accessLogRepository.findByEmployeeIdAndTimestampBetween(
                employeeId,
                startDate,
                endDate,
                accessType,
                areaId
        );
        return ResponseEntity.ok(accessLogs);
    }
}
