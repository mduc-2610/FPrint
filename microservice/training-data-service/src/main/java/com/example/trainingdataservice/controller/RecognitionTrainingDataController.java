package com.example.trainingdataservice.controller;

import com.example.trainingdataservice.model.RecognitionTrainingData;
import com.example.trainingdataservice.service.RecognitionTrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recognition-training-data")
public class RecognitionTrainingDataController {
    @Autowired
    private RecognitionTrainingDataService recognitionTrainingDataService;

    @GetMapping
    public ResponseEntity<List<RecognitionTrainingData>> getAllRecognitionData() {
        return ResponseEntity.ok(recognitionTrainingDataService.getAllRecognitionData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecognitionTrainingData> getRecognitionDataById(@PathVariable String id) {
        return recognitionTrainingDataService.getRecognitionDataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/training-data/{trainingDataId}")
    public ResponseEntity<List<RecognitionTrainingData>> getByTrainingDataId(@PathVariable String trainingDataId) {
        return ResponseEntity.ok(recognitionTrainingDataService.getRecognitionDataByTrainingDataId(trainingDataId));
    }

    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<RecognitionTrainingData>> getByModelId(@PathVariable String modelId) {
        return ResponseEntity.ok(recognitionTrainingDataService.getRecognitionDataByModelId(modelId));
    }

    @PostMapping("/training-data/{trainingDataId}")
    public ResponseEntity<RecognitionTrainingData> createRecognitionData(
            @PathVariable String trainingDataId,
            @RequestBody RecognitionTrainingData data) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(recognitionTrainingDataService.createRecognitionData(data, trainingDataId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecognitionTrainingData> updateRecognitionData(
            @PathVariable String id,
            @RequestBody RecognitionTrainingData data) {

        if (!recognitionTrainingDataService.getRecognitionDataById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        data.setId(id);
        return ResponseEntity.ok(recognitionTrainingDataService.updateRecognitionData(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecognitionData(@PathVariable String id) {
        recognitionTrainingDataService.deleteRecognitionData(id);
        return ResponseEntity.noContent().build();
    }
}
