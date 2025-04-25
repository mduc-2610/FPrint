package com.example.accesscontrolservice.controller;

import com.example.accesscontrolservice.model.AccessLog;
import com.example.accesscontrolservice.service.AccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/access-logs")
public class AccessLogController {
    @Autowired
    private AccessLogService accessLogService;

    @GetMapping
    public ResponseEntity<List<AccessLog>> getAllAccessLogs() {
        return ResponseEntity.ok(accessLogService.getAllAccessLogs());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AccessLog>> getAccessLogsByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(accessLogService.getAccessLogsByEmployeeId(employeeId));
    }

    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<AccessLog>> getAccessLogsByAreaId(@PathVariable String areaId) {
        return ResponseEntity.ok(accessLogService.getAccessLogsByAreaId(areaId));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<AccessLog>> getAccessLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(accessLogService.getAccessLogsByTimeRange(start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessLog> getAccessLogById(@PathVariable String id) {
        return accessLogService.getAccessLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AccessLog> createAccessLog(@RequestBody AccessLog accessLog) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessLogService.createAccessLog(accessLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccessLog(@PathVariable String id) {
        accessLogService.deleteAccessLog(id);
        return ResponseEntity.noContent().build();
    }
}
