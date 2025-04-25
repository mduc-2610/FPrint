package com.example.biometricsservice.controller;

import com.example.biometricsservice.model.Recognition;
import com.example.biometricsservice.model.RecognitionResult;
import com.example.biometricsservice.repository.RecognitionRepository;
import com.example.biometricsservice.service.FingerprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recognitions")
public class RecognitionController {

    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private RecognitionRepository recognitionRepository;

    @GetMapping
    public ResponseEntity<List<Recognition>> getAllRecognitions() {
        return ResponseEntity.ok(recognitionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recognition> getRecognitionById(@PathVariable String id) {
        return recognitionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Recognition>> getRecognitionsByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(recognitionRepository.findByEmployeeId(employeeId));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Recognition>> getRecognitionsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(recognitionRepository.findByTimestampBetween(start, end));
    }

    @PostMapping("/recognize")
    public ResponseEntity<RecognitionResult> recognizeFingerprint(
            @RequestParam("image") MultipartFile image,
            @RequestParam("segmentationModelId") String segmentationModelId,
            @RequestParam("recognitionModelId") String recognitionModelId) {

        try {
            RecognitionResult result = fingerprintService.recognizeFingerprint(
                    image,
                    segmentationModelId,
                    recognitionModelId
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/access")
    public ResponseEntity<Object> logAccess(
            @RequestBody Map<String, Object> request) {

        try {
            String employeeId = (String) request.get("employeeId");
            String areaId = (String) request.get("areaId");
            String accessType = (String) request.get("accessType");
            boolean isMatched = (boolean) request.get("isMatched");
            double confidence = (double) request.get("confidence");
            String segmentationModelId = (String) request.get("segmentationModelId");
            String recognitionModelId = (String) request.get("recognitionModelId");

            Object accessLog = fingerprintService.createAccessLog(
                    employeeId,
                    areaId,
                    accessType,
                    isMatched,
                    confidence,
                    segmentationModelId,
                    recognitionModelId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(accessLog);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
