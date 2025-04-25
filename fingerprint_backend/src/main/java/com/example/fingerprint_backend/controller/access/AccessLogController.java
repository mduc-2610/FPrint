package com.example.fingerprint_backend.controller.access;

import com.example.fingerprint_backend.model.access.AccessLog;
import com.example.fingerprint_backend.model.auth.Employee;
import com.example.fingerprint_backend.repository.access.AccessLogRepository;
import com.example.fingerprint_backend.repository.auth.EmployeeRepository;
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

    @GetMapping
    public List<AccessLog> getAllAccessLogs() {
        return accessLogRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessLog> getAccessLogById(@PathVariable String id) {
        Optional<AccessLog> accessLog = accessLogRepository.findById(id);
        return accessLog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public List<AccessLog> getAccessLogsByEmployee(@PathVariable String employeeId) {
        return accessLogRepository.findByEmployeeId(employeeId);
    }

    @GetMapping("/area/{areaId}")
    public List<AccessLog> getAccessLogsByArea(@PathVariable String areaId) {
        return accessLogRepository.findByAreaId(areaId);
    }

    @GetMapping("/timestamp")
    public List<AccessLog> getAccessLogsByTimeStamp(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return accessLogRepository.findByTimestampBetween(start, end);
    }

    @PostMapping
    public AccessLog createAccessLog(@RequestBody AccessLog accessLog) {
        return accessLogRepository.save(accessLog);
    }

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
