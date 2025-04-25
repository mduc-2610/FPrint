package com.example.fingerprint_backend.model.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatistics {
    private String employeeId;
    private String fullName;
    private long totalAccesses;
    private LocalDateTime firstAccessTime;
    private LocalDateTime lastAccessTime;
}