package com.example.modelmanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelStatistics {
    private String modelId;
    private String modelName;
    private int totalUsage;
    private float averageConfidence;
    private LocalDateTime lastUsed;
}
