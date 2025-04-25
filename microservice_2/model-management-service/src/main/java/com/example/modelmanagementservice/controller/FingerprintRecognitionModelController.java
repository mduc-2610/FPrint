package com.example.modelmanagementservice.controller;

import com.example.modelmanagementservice.model.FingerprintRecognitionModel;
import com.example.modelmanagementservice.model.FingerprintSegmentationModel;
import com.example.modelmanagementservice.model.ModelStatistics;
import com.example.modelmanagementservice.repository.FingerprintRecognitionModelRepository;
import com.example.modelmanagementservice.repository.FingerprintSegmentationModelRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fingerprint-recognition-model")
public class FingerprintRecognitionModelController extends ModelController<FingerprintRecognitionModel> {
    private final FingerprintRecognitionModelRepository repository;

    @Autowired
    public FingerprintRecognitionModelController(FingerprintRecognitionModelRepository repository) {
        this.repository = repository;
        this.modelRepository = repository;
    }

     @Override
    protected List<Map<String, Object>> getUsageData(String modelId) {
        return biometricsClient.getRecognitionsByRecognitionModelId(modelId);
    }
    
    @GetMapping
    public ResponseEntity<List<FingerprintRecognitionModel>> getAllModels() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FingerprintRecognitionModel> getModelById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}