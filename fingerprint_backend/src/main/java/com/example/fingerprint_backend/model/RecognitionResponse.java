package com.example.fingerprint_backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecognitionResponse {
    private boolean matched;
    private double confidence;
    private AccessLog accessLog;
    private boolean authorized;
    private Boolean accessable;
    private String employeeId;
    private Employee employee;
}