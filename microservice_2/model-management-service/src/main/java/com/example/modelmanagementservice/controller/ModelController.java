package com.example.modelmanagementservice.controller;

import com.example.modelmanagementservice.client.BiometricsClient;
import com.example.modelmanagementservice.model.Model;
import com.example.modelmanagementservice.model.ModelStatistics;
import com.example.modelmanagementservice.repository.ModelRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public abstract class ModelController<T extends Model> {
    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    
    @Autowired
    protected ModelRepository<T> modelRepository;
    
    @Autowired
    protected BiometricsClient biometricsClient;
    
    @GetMapping("/statistics")
    public ResponseEntity<List<ModelStatistics>> getAllModelStatistics() {
        try {
            List<T> models = modelRepository.findAll();
            List<ModelStatistics> statistics = new ArrayList<>();
            
            for (T model : models) {
                ModelStatistics modelStats = calculateModelStatistics(model);
                statistics.add(modelStats);
            }
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error retrieving model statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ModelStatistics> getModelStatisticsById(@PathVariable String id) {
        try {
            Optional<T> modelOpt = modelRepository.findById(id);
            
            if (modelOpt.isPresent()) {
                ModelStatistics modelStats = calculateModelStatistics(modelOpt.get());
                return ResponseEntity.ok(modelStats);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving model statistics for id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    protected abstract List<Map<String, Object>> getUsageData(String modelId);

    protected ModelStatistics calculateModelStatistics(T model) {
        String modelId = model.getId();
        List<Map<String, Object>> usageData = getUsageData(modelId);
        
        int totalUsage = usageData.size();
        float totalConfidence = 0f;
        LocalDateTime lastUsed = null;
        
        for (Map<String, Object> usage : usageData) {
            if (usage.containsKey("confidence")) {
                Object confidenceObj = usage.get("confidence");
                if (confidenceObj instanceof Number) {
                    totalConfidence += ((Number) confidenceObj).floatValue();
                }
            }
        }
        
        float averageConfidence = totalUsage > 0 ? totalConfidence / totalUsage : 0f;
        
        return ModelStatistics.builder()
                .model(model)
                .totalUsage(totalUsage)
                .averageConfidence(averageConfidence)
                .build();
    }
}
