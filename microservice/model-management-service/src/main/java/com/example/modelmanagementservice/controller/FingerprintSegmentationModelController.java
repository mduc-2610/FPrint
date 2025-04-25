package com.example.modelmanagementservice.controller;

import com.example.modelmanagementservice.model.FingerprintSegmentationModel;
import com.example.modelmanagementservice.service.FingerprintSegmentationModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fingerprint-segmentation-models")
public class FingerprintSegmentationModelController {
    @Autowired
    private FingerprintSegmentationModelService modelService;

    @GetMapping
    public ResponseEntity<List<FingerprintSegmentationModel>> getAllModels() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FingerprintSegmentationModel> getModelById(@PathVariable String id) {
        return modelService.getModelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accuracy/{threshold}")
    public ResponseEntity<List<FingerprintSegmentationModel>> getModelsByAccuracy(@PathVariable float threshold) {
        return ResponseEntity.ok(modelService.findByAccuracyGreaterThan(threshold));
    }

    @GetMapping("/created-after")
    public ResponseEntity<List<FingerprintSegmentationModel>> getModelsCreatedAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(modelService.findByCreatedAfter(date));
    }

    @PostMapping
    public ResponseEntity<FingerprintSegmentationModel> createModel(@RequestBody FingerprintSegmentationModel model) {
        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.createModel(model));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FingerprintSegmentationModel> updateModel(@PathVariable String id, @RequestBody FingerprintSegmentationModel model) {
        if (!modelService.getModelById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        model.setId(id);
        return ResponseEntity.ok(modelService.updateModel(model));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable String id) {
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
