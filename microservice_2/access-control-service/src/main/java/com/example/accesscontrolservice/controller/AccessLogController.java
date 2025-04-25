package com.example.accesscontrolservice.controller;

import com.example.accesscontrolservice.model.AccessLog;
import com.example.accesscontrolservice.repository.AccessLogRepository;
import com.example.accesscontrolservice.repository.AreaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/access-log")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogRepository accessLogRepository;

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

    @GetMapping("/employee/{employeeId}/access-log")
    public ResponseEntity<List<AccessLog>> getEmployeeAccessLogs(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<AccessLog> accessLogs = accessLogRepository.findByEmployeeIdAndTimestampBetween(
            employeeId, startDate, endDate, null, null);

        return ResponseEntity.ok(accessLogs);
    }


    @GetMapping("/time-range")
    public ResponseEntity<List<AccessLog>> getAccessLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<AccessLog> accessLogs = accessLogRepository.findByTimestampBetween(start, end);
        return ResponseEntity.ok(accessLogs);
    }

    @PostMapping
    public ResponseEntity<?> createAccessLog(@RequestBody AccessLog accessLog) {
        accessLog.setTimestamp(LocalDateTime.now());
        AccessLog saved = accessLogRepository.save(accessLog);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/recognition-id")
    public ResponseEntity<?> updateRecognitionId(
            @PathVariable String id,
            @RequestBody String recognitionId) {
        Optional<AccessLog> optionalLog = accessLogRepository.findById(id);

        if (optionalLog.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AccessLog accessLog = optionalLog.get();
        accessLog.setRecognitionId(recognitionId);
        accessLogRepository.save(accessLog);

        return ResponseEntity.ok(accessLog);
    }
}