package com.example.fingerprint_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelStatistics {
    private Model model;
    private int totalUsage;
    private float averageConfidence;
}
