package com.example.biometricsservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecognitionResult {
    private final String employeeId;
    private final double confidence;
    private static final double MATCH_THRESHOLD = 0.7;

    public String getEmployeeId() {
        return employeeId;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isMatch() {
        return employeeId != null && confidence >= MATCH_THRESHOLD;
    }
}
