package com.example.trainingdataservice.controller;

import com.example.trainingdataservice.model.SegmentationTrainingData;
import com.example.trainingdataservice.service.SegmentationTrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segmentation-training-data")
public class SegmentationTrainingDataController {
    @Autowired
    private SegmentationTrainingDataService segmentationTrainingDataService;

    @GetMapping
    public ResponseEntity<List<SegmentationTrainingData>> getAllSegmentationData() {
        return ResponseEntity.ok(segmentationTrainingDataService.getAllSegmentationData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SegmentationTrainingData> getSegmentationDataById(@PathVariable String id) {
        return segmentationTrainingDataService.getSegmentationDataById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/training-data/{trainingDataId}")
    public ResponseEntity<List<SegmentationTrainingData>> getByTrainingDataId(@PathVariable String trainingDataId) {
        return ResponseEntity.ok(segmentationTrainingDataService.getSegmentationDataByTrainingDataId(trainingDataId));
    }

    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<SegmentationTrainingData>> getByModelId(@PathVariable String modelId) {
        return ResponseEntity.ok(segmentationTrainingDataService.getSegmentationDataByModelId(modelId));
    }

    @PostMapping("/training-data/{trainingDataId}")
    public ResponseEntity<SegmentationTrainingData> createSegmentationData(
            @PathVariable String trainingDataId,
            @RequestBody SegmentationTrainingData data) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(segmentationTrainingDataService.createSegmentationData(data, trainingDataId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SegmentationTrainingData> updateSegmentationData(
            @PathVariable String id,
            @RequestBody SegmentationTrainingData data) {

        if (!segmentationTrainingDataService.getSegmentationDataById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        data.setId(id);
        return ResponseEntity.ok(segmentationTrainingDataService.updateSegmentationData(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSegmentationData(@PathVariable String id) {
        segmentationTrainingDataService.deleteSegmentationData(id);
        return ResponseEntity.noContent().build();
    }
}

