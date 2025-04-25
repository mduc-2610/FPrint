package com.example.trainingdataservice.controller;

import com.example.trainingdataservice.model.TrainingData;
import com.example.trainingdataservice.service.TrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/training-data")
public class TrainingDataController {
    @Autowired
    private TrainingDataService trainingDataService;

    @GetMapping
    public ResponseEntity<List<TrainingData>> getAllTrainingData() {
        return ResponseEntity.ok(trainingDataService.getAllTrainingData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingData> getTrainingDataById(@PathVariable String id) {
        return trainingDataService.getTrainingDataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TrainingData>> searchTrainingData(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) Integer minSampleCount) {

        if (name != null) {
            return ResponseEntity.ok(trainingDataService.findByName(name));
        } else if (purpose != null) {
            return ResponseEntity.ok(trainingDataService.findByPurpose(purpose));
        } else if (createdAfter != null) {
            return ResponseEntity.ok(trainingDataService.findByCreatedAfter(createdAfter));
        } else if (minSampleCount != null) {
            return ResponseEntity.ok(trainingDataService.findBySampleCountGreaterThan(minSampleCount));
        } else {
            return ResponseEntity.ok(trainingDataService.getAllTrainingData());
        }
    }

    @PostMapping
    public ResponseEntity<TrainingData> createTrainingData(@RequestBody TrainingData trainingData) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingDataService.createTrainingData(trainingData));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingData> updateTrainingData(@PathVariable String id, @RequestBody TrainingData trainingData) {
        if (!trainingDataService.getTrainingDataById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        trainingData.setId(id);
        return ResponseEntity.ok(trainingDataService.updateTrainingData(trainingData));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingData(@PathVariable String id) {
        trainingDataService.deleteTrainingData(id);
        return ResponseEntity.noContent().build();
    }
}
